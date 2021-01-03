#!/bin/bash

set -e

cd server

echo "Running deployment server"
PAPER_MEMORY=1G ./start.sh &
SERVER_PID=$!
echo "Launched server, PID is $SERVER_PID"

# A normal invocation of "kill" does not kill child processes, which would leave the Paper server
# itself orphaned
function rkill {
  for child in $(pgrep -P $1); do
    rkill $child "$2"
  done

  echo rkill: kill $2 $1
  kill $2 $1
}

trap "echo \"Terminating server\"; rkill $SERVER_PID -9" 0

cd ../bot
./start.sh
