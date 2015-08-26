#!/bin/bash

set -eo pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

alexa_dispatcher_tmpdir() {
  cd $DIR/..
  mkdir -p tmp
  cd tmp

  pwd
}

alexa_dispatcher_pidfile() {
  echo "$(alexa_dispatcher_tmpdir)/alexa_dispatcher.pid"
}

alexa_dispatcher_locate() {
  pidfile=$(alexa_dispatcher_pidfile)
  if [[ -e "$pidfile" ]] && [[ $(ps -p $(cat "$pidfile") -o 'pid=' | wc -l) -gt 0 ]]; then
    cat "$pidfile"
  fi
}

alexa_dispatcher_logdir() {
  cd $DIR/..
  mkdir -p log
  cd log

  pwd
}

main() {
  alexa_dispatcher_locate
}

[[ "$0" == "$BASH_SOURCE" ]] && main