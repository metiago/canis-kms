#!/bin/bash

set -eu

# The client build and unit tests provision their own test fixtures. Publishing
# requires separate authorization and externally supplied Maven credentials.
mvn clean install
