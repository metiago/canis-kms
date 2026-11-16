#!/bin/sh
set -eu
repo_root=$(CDPATH= cd "$(dirname "$0")/../.." && pwd)
cd "$repo_root"
git rev-parse HEAD
mvn --version
mvn -q -pl server -am -Dtest=ReviewerWorkflowTest -Dsurefire.failIfNoSpecifiedTests=false test
