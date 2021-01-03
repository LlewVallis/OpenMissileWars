#!/bin/sh

set -e

./create-deployment.sh

cd test

if [ -e server ]; then
  echo "Cleaning previous server"
  rm -r server
fi

echo "Copying deployment server"
cp -R ../deployment/server .

cd server

SERVER_PORT=36676
RCON_PORT=36686

echo "Configuring deployment server"
echo online-mode=false >> configs/server.properties
echo server-port=$SERVER_PORT >> configs/server.properties
echo query.port=$SERVER_PORT >> configs/server.properties
echo rcon.port=$RCON_PORT >> configs/server.properties
cat > configs/ops.json <<- EOM
[
 {
   "uuid": "32758507-c39c-3d09-a0a6-cddb453cab03",
   "name": "integration-bot",
   "level": 4,
   "bypassesPlayerLimit": false
 }
]
EOM



cd ..

./test.sh
