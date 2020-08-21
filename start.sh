#!/bin/sh

set -e

./build.sh

echo "Executing server"
cd server
./start.sh
