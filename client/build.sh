#!/bin/bash

export CANIS_SERVER_PORT=3307
export CANIS_USERNAME=admin
export CANIS_PASSWORD=123

mvn clean install

mvn deploy