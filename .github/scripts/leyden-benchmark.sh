#!/usr/bin/env bash
set -euo pipefail
umask 077

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
results="$repo_root/Leyden"
jar="$repo_root/server/target/canis-jar-with-dependencies.jar"
cache="$results/canis.aot"
run_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/canis-leyden.XXXXXXXX")"
java_pid=''

cleanup() {
  if [[ -n "$java_pid" ]] && kill -0 "$java_pid" 2>/dev/null; then
    kill -TERM "$java_pid" 2>/dev/null || true
    wait "$java_pid" 2>/dev/null || true
  fi
  if [[ "$run_dir" == "${RUNNER_TEMP:-/tmp}"/canis-leyden.* ]]; then
    rm -rf -- "$run_dir"
  fi
}
trap cleanup EXIT

test -f "$jar"
mkdir -p "$results"
java -version > "$results/java-version.txt" 2>&1
printf 'mode,total_classes,shared_archive_classes,other_classes,ready_ms\n' > "$results/summary.csv"

# The runner creates disposable credentials. None of these files are uploaded.
chmod 700 "$run_dir"
head -c 32 /dev/urandom | base64 > "$run_dir/master.key"
chmod 600 "$run_dir/master.key"
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 2 \
  -storetype PKCS12 -keystore "$run_dir/server.p12" -storepass changeit -keypass changeit \
  -dname 'CN=localhost' -noprompt > "$results/fixture-setup.log" 2>&1
keytool -exportcert -alias server -keystore "$run_dir/server.p12" -storepass changeit \
  -file "$run_dir/server.cer" >> "$results/fixture-setup.log" 2>&1
keytool -importcert -alias client -storetype PKCS12 -keystore "$run_dir/trust.p12" \
  -storepass changeit -file "$run_dir/server.cer" -noprompt >> "$results/fixture-setup.log" 2>&1

export CANIS_USERNAME=benchmark
export CANIS_PASSWORD=benchmark-only
export CANIS_PORT=3307
export CANIS_SECRET_KEY="$run_dir/master.key"
export CANIS_TLS_KEYSTORE="$run_dir/server.p12"
export CANIS_TLS_KEYSTORE_PASSWORD=changeit
export CANIS_TLS_TRUSTSTORE="$run_dir/trust.p12"
export CANIS_TLS_TRUSTSTORE_PASSWORD=changeit

(cd "$run_dir" && java -jar "$jar" --init-db) > "$results/init.log" 2>&1

run_server() {
  local mode="$1"
  local raw_log="$run_dir/classes-$mode.log"
  local class_log="$results/classes-$mode.log"
  local server_log="$results/server-$mode.log"
  local aot_log="$results/aot-$mode.log"
  local vm_args=("-Xlog:class+load=info:file=$raw_log")
  case "$mode" in
    baseline) ;;
    train) vm_args+=("-Xlog:aot=info:file=$aot_log" "-XX:AOTCacheOutput=$cache") ;;
    aot) vm_args+=("-Xlog:aot=info:file=$aot_log" '-XX:AOTMode=on' "-XX:AOTCache=$cache") ;;
  esac

  local start_ms ready_ms ready=false
  start_ms="$(date +%s%3N)"
  (cd "$run_dir" && exec java "${vm_args[@]}" -jar "$jar") > "$server_log" 2>&1 &
  java_pid=$!
  for _ in {1..300}; do
    if grep -q 'TLS server is listening on port' "$server_log"; then
      ready=true
      break
    fi
    if ! kill -0 "$java_pid" 2>/dev/null; then
      echo "$mode: server exited before readiness" >&2
      cat "$server_log" >&2
      [[ ! -f "$aot_log" ]] || cat "$aot_log" >&2
      return 1
    fi
    sleep 0.1
  done
  if [[ "$ready" != true ]]; then
    echo "$mode: server did not become ready within 30 seconds" >&2
    cat "$server_log" >&2
    return 1
  fi

  ready_ms="$(($(date +%s%3N) - start_ms))"
  cp "$raw_log" "$class_log"
  local total shared
  total="$(grep -c '\[class,load\].*source:' "$class_log" || true)"
  shared="$(grep -c '\[class,load\].*source: shared objects file' "$class_log" || true)"
  printf '%s,%s,%s,%s,%s\n' "$mode" "$total" "$shared" "$((total - shared))" "$ready_ms" >> "$results/summary.csv"
  echo "$mode: $total classes ($shared shared), ready in about $ready_ms ms"

  kill -TERM "$java_pid"
  wait "$java_pid" || true
  java_pid=''
  if [[ "$mode" == train ]]; then
    for _ in {1..50}; do
      [[ -s "$cache" ]] && break
      sleep 0.1
    done
    if [[ ! -s "$cache" ]]; then
      echo 'AOT training did not create canis.aot' >&2
      cat "$aot_log" >&2
      return 1
    fi
  fi
}

run_server baseline
run_server train
run_server aot
cat "$results/summary.csv"

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo '## CANIS class loading on Linux / JDK 25'
    echo
    echo '| Mode | Classes | Shared archive | Other | Ready (approx. ms) |'
    echo '| --- | ---: | ---: | ---: | ---: |'
    tail -n +2 "$results/summary.csv" | while IFS=, read -r mode total shared other ready_ms; do
      echo "| $mode | $total | $shared | $other | $ready_ms |"
    done
  } >> "$GITHUB_STEP_SUMMARY"
fi
