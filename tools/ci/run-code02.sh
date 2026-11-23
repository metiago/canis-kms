#!/bin/sh
set -eu
repo_root=$(CDPATH= cd "$(dirname "$0")/../.." && pwd)
cd "$repo_root"
mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package
mkdir -p server/target/code02-classes
javac -cp client/target/classes -d server/target/code02-classes docs/research/Code02Harness.java
java -cp "server/target/code02-classes:client/target/classes" Code02Harness "$@"
