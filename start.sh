#!/usr/bin/env bash

# Run from Git Bash on Windows. Each mode uses the same JDK, JAR, and server config.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_DIR="$ROOT_DIR/.run"
OUTPUT_DIR="$ROOT_DIR/Leyden"
RUN_XML="$RUN_DIR/Server.run.xml"
LOGBACK_XML="$ROOT_DIR/server/src/main/resources/logback.xml"
JAR_PATH="$ROOT_DIR/server/target/canis-jar-with-dependencies.jar"
JDK_WINDOWS='C:\Users\TSIG\workspace\jdk-25.0.5+8'

usage() {
  echo "Usage: $0 [baseline|train|aot]"
  echo "  baseline  Run normally and count classes loaded before exit"
  echo "  train     Run with -Xlog:aot -XX:AOTCacheOutput=canis.aot"
  echo "  aot       Run with -XX:AOTCache=canis.aot and count classes"
  echo "Stop the server with Ctrl+C to print the class counts."
  echo "Logs and AOT files are saved in $ROOT_DIR/Leyden."
}

mode="${1:-baseline}"
if [[ $# -gt 1 || ! "$mode" =~ ^(baseline|train|aot)$ ]]; then
  usage
  exit 2
fi

if ! command -v cygpath >/dev/null 2>&1; then
  echo "Run this script from Git Bash on Windows (cygpath is required)." >&2
  exit 1
fi
export JAVA_HOME="$(cygpath -u "$JDK_WINDOWS")"
export PATH="$JAVA_HOME/bin:$PATH"
JAVA="$JAVA_HOME/bin/java.exe"
if [[ ! -x "$JAVA" || ! -f "$JAR_PATH" || ! -f "$RUN_XML" || ! -f "$LOGBACK_XML" ]]; then
  echo "Missing JDK, server JAR, or server configuration. Build the JAR with 'mvn -pl server -am package' first." >&2
  exit 1
fi

# The IntelliJ server configuration uses .run as its working directory.
mkdir -p "$OUTPUT_DIR/server-logs"
sed 's#logs/application#../Leyden/server-logs/application#g' "$LOGBACK_XML" > "$OUTPUT_DIR/logback.xml"
cd "$RUN_DIR"
while IFS= read -r line; do
  if [[ $line =~ \<env[[:space:]]+name=\"([^\"]+)\"[[:space:]]+value=\"([^\"]*)\" ]]; then
    export "${BASH_REMATCH[1]}=${BASH_REMATCH[2]}"
  elif [[ $line =~ \<env[[:space:]]+value=\"([^\"]*)\"[[:space:]]+name=\"([^\"]+)\" ]]; then
    export "${BASH_REMATCH[2]}=${BASH_REMATCH[1]}"
  fi
done < "$RUN_XML"

class_log="../Leyden/classes-${mode}.log"
aot_log="../Leyden/aot-${mode}.log"
cache_file="../Leyden/canis.aot"
vm_args=("-Dlogback.configurationFile=../Leyden/logback.xml" "-Xlog:class+load=info:file=$class_log")
case "$mode" in
  baseline) ;;
  train) vm_args+=("-Xlog:aot=info:file=$aot_log" "-XX:AOTCacheOutput=$cache_file") ;;
  aot)
    if [[ ! -f "$cache_file" ]]; then
      echo "Missing $OUTPUT_DIR/canis.aot. Run '$0 train' first." >&2
      exit 1
    fi
    vm_args+=("-Xlog:aot=info:file=$aot_log" "-XX:AOTMode=on" "-XX:AOTCache=$cache_file")
    ;;
esac

summarize() {
  if [[ -f "$class_log" ]]; then
    local total shared
    total=$(grep -c '\[class,load\].*source:' "$class_log" || true)
    shared=$(grep -c '\[class,load\].*source: shared objects file' "$class_log" || true)
    echo "Classes loaded: $total (from shared archive: $shared; other: $((total - shared)))"
    echo "Class log: $OUTPUT_DIR/classes-${mode}.log"
  fi
  if [[ "$mode" == train ]]; then
    if [[ -s "$cache_file" ]]; then
      echo "AOT cache: $OUTPUT_DIR/canis.aot"
    else
      echo "AOT cache was not created. Check $OUTPUT_DIR/aot-train.log." >&2
    fi
  fi
  if [[ "$mode" != baseline && -f "$aot_log" ]]; then
    echo "AOT log: $OUTPUT_DIR/aot-${mode}.log"
  fi
}
trap summarize EXIT

echo "Mode: $mode | Java: $JAVA | Working directory: $RUN_DIR"
echo "Stop with Ctrl+C when the server is ready."
"$JAVA" "${vm_args[@]}" -jar "$JAR_PATH"
