#!/usr/bin/env bash
set -euo pipefail
umask 077

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
results="$repo_root/Leyden"
jar="$repo_root/server/target/canis-jar-with-dependencies.jar"
client_classes="$repo_root/client/target/classes"
cache="$results/canis.aot"
run_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/canis-leyden.XXXXXXXX")"
java_pid=''
client_pid=''
client_reply=''

cleanup() {
  if [[ -n "$java_pid" ]] && kill -0 "$java_pid" 2>/dev/null; then
    kill -TERM "$java_pid" 2>/dev/null || true
    wait "$java_pid" 2>/dev/null || true
  fi
  if [[ -n "$client_pid" ]] && kill -0 "$client_pid" 2>/dev/null; then
    kill -TERM "$client_pid" 2>/dev/null || true
    wait "$client_pid" 2>/dev/null || true
  fi
  if [[ "$run_dir" == "${RUNNER_TEMP:-/tmp}"/canis-leyden.* ]]; then
    rm -rf -- "$run_dir"
  fi
}
trap cleanup EXIT

test -f "$jar"
test -d "$client_classes"
mkdir -p "$results" "$run_dir/client-classes"
java -version > "$results/java-version.txt" 2>&1
printf 'mode,iteration,total_classes,shared_archive_classes,other_classes,ready_ms,first_req_ms,warmup_50_ms\n' > "$results/results-runs.csv"

# Disposable credentials and database stay in the runner's private temporary directory.
chmod 700 "$run_dir"
head -c 32 /dev/urandom | base64 > "$run_dir/master.key"
chmod 600 "$run_dir/master.key"
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 2 \
  -storetype PKCS12 -keystore "$run_dir/server.p12" -storepass changeit -keypass changeit \
  -dname 'CN=localhost' -ext 'SAN=dns:localhost,ip:127.0.0.1' -noprompt > "$results/fixture-setup.log" 2>&1
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -validity 2 \
  -storetype PKCS12 -keystore "$run_dir/client.p12" -storepass changeit -keypass changeit \
  -dname 'CN=benchmark-client' -noprompt >> "$results/fixture-setup.log" 2>&1
keytool -exportcert -alias server -keystore "$run_dir/server.p12" -storepass changeit \
  -file "$run_dir/server.cer" >> "$results/fixture-setup.log" 2>&1
keytool -exportcert -alias client -keystore "$run_dir/client.p12" -storepass changeit \
  -file "$run_dir/client.cer" >> "$results/fixture-setup.log" 2>&1
keytool -importcert -alias client -storetype PKCS12 -keystore "$run_dir/server-trust.p12" \
  -storepass changeit -file "$run_dir/client.cer" -noprompt >> "$results/fixture-setup.log" 2>&1
keytool -importcert -alias server -storetype PKCS12 -keystore "$run_dir/client-trust.p12" \
  -storepass changeit -file "$run_dir/server.cer" -noprompt >> "$results/fixture-setup.log" 2>&1

export CANIS_USERNAME=benchmark
export CANIS_PASSWORD=benchmark-only
export CANIS_PORT=3307
export CANIS_SECRET_KEY="$run_dir/master.key"
export CANIS_TLS_KEYSTORE="$run_dir/server.p12"
export CANIS_TLS_KEYSTORE_PASSWORD=changeit
export CANIS_TLS_TRUSTSTORE="$run_dir/server-trust.p12"
export CANIS_TLS_TRUSTSTORE_PASSWORD=changeit
export CANIS_SERVER_HOST=localhost
export CANIS_SERVER_PORT="$CANIS_PORT"
export CANIS_CLIENT_TLS_KEYSTORE="$run_dir/client.p12"
export CANIS_CLIENT_TLS_KEYSTORE_PASSWORD=changeit
export CANIS_CLIENT_TLS_TRUSTSTORE="$run_dir/client-trust.p12"
export CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD=changeit

(cd "$run_dir" && java -jar "$jar" --init-db) > "$results/init.log" 2>&1
javac -cp "$client_classes" -d "$run_dir/client-classes" "$repo_root/.github/scripts/LeydenClient.java"
coproc CLIENT { exec java -cp "$run_dir/client-classes:$client_classes" LeydenClient 2> "$results/client.log"; }
client_pid=$CLIENT_PID

request_client() {
  local command="$1"
  printf '%s\n' "$command" >&"${CLIENT[1]}"
  if ! IFS= read -r -t 180 client_reply <&"${CLIENT[0]}"; then
    echo "Client did not complete '$command'" >&2
    cat "$results/client.log" >&2
    return 1
  fi
  if [[ "$client_reply" == error* ]]; then
    cat "$results/client.log" >&2
    return 1
  fi
}

run_server() {
  local mode="$1" iteration="$2" label
  if [[ "$mode" == train ]]; then
    label=train
  else
    printf -v label '%s-%02d' "$mode" "$iteration"
  fi
  local raw_log="$run_dir/classes-$label.log"
  local class_log="$results/classes-$label.log"
  local server_log="$results/server-$label.log"
  local aot_log="$results/aot-$label.log"
  local vm_args=("-Xlog:class+load=info:file=$raw_log")
  case "$mode" in
    baseline) ;;
    train) vm_args+=("-Xlog:aot=info:file=$aot_log" '-XX:AOTMode=record' "-XX:AOTCacheOutput=$cache") ;;
    aot) vm_args+=("-Xlog:aot=info:file=$aot_log" '-XX:AOTMode=on' "-XX:AOTCache=$cache") ;;
  esac

  local start_ms ready_ms ready=false
  start_ms="$(date +%s%3N)"
  (cd "$run_dir" && exec java "${vm_args[@]}" -jar "$jar") > "$server_log" 2>&1 &
  java_pid=$!
  for _ in {1..1500}; do
    if grep -q 'TLS server is listening on port' "$server_log"; then
      ready=true
      break
    fi
    if ! kill -0 "$java_pid" 2>/dev/null; then
      echo "$label: server exited before readiness" >&2
      cat "$server_log" >&2
      [[ ! -f "$aot_log" ]] || cat "$aot_log" >&2
      return 1
    fi
    sleep 0.02
  done
  if [[ "$ready" != true ]]; then
    echo "$label: server did not become ready within 30 seconds" >&2
    cat "$server_log" >&2
    return 1
  fi

  ready_ms="$(($(date +%s%3N) - start_ms))"
  cp "$raw_log" "$class_log"
  local total shared
  total="$(grep -c '\[class,load\].*source:' "$class_log" || true)"
  shared="$(grep -c '\[class,load\].*source: shared objects file' "$class_log" || true)"

  if [[ "$mode" == train ]]; then
    request_client train
    [[ "$client_reply" == 'trained,40' ]] || { echo "Unexpected training reply: $client_reply" >&2; return 1; }
  else
    request_client measure
    local reply_type first_req_ms warmup_50_ms
    IFS=, read -r reply_type first_req_ms warmup_50_ms <<< "$client_reply"
    if [[ "$reply_type" != measure || ! "$first_req_ms" =~ ^[0-9]+[.][0-9]{3}$ || ! "$warmup_50_ms" =~ ^[0-9]+[.][0-9]{3}$ ]]; then
      echo "Unexpected measurement reply: $client_reply" >&2
      return 1
    fi
    printf '%s,%d,%d,%d,%d,%d,%s,%s\n' "$mode" "$iteration" "$total" "$shared" "$((total - shared))" "$ready_ms" "$first_req_ms" "$warmup_50_ms" >> "$results/results-runs.csv"
    echo "$label: $total classes ($shared shared), ready ${ready_ms} ms, first request ${first_req_ms} ms, 50 requests ${warmup_50_ms} ms"
  fi

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

run_server train 0
for iteration in {1..10}; do
  if (( iteration % 2 == 1 )); then
    run_server baseline "$iteration"
    run_server aot "$iteration"
  else
    run_server aot "$iteration"
    run_server baseline "$iteration"
  fi
done

python3 "$repo_root/.github/scripts/summarize-leyden.py" "$results/results-runs.csv" "$results/results-summary.csv"
