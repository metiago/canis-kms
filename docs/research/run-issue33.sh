#!/bin/sh
set -eu
repo_root=$(CDPATH= cd "$(dirname "$0")/../.." && pwd)
cd "$repo_root"
mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package
mkdir -p server/target/issue33-classes
javac -cp client/target/classes -d server/target/issue33-classes docs/research/Code02Harness.java docs/research/Issue33Harness.java
java -cp "server/target/issue33-classes:client/target/classes" Issue33Harness "$@"
