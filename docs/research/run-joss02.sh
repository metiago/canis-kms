#!/bin/sh
set -eu
repo_root=$(CDPATH= cd "$(dirname "$0")/../.." && pwd)
cd "$repo_root"
printf '%s\n' 'JOSS-02 source revision:'
git rev-parse HEAD
printf '%s\n' 'JOSS-02 working-tree changes:'
git status --short
printf '%s\n' 'Operating system:'
uname -a
printf '%s\n' 'Maven and Maven Java runtime:'
mvn --version
printf '%s\n' 'Full reactor test suite:'
mvn -q test
printf '%s\n' 'Test reports: client/target/surefire-reports and server/target/surefire-reports'
