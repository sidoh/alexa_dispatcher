#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR/..

mvn clean compile exec:java -Dexec.mainClass="org.sidoh.alexa_dispatcher.AlexaDispatcherServer" -Dexec.args="$DIR/../config/config.yml"
