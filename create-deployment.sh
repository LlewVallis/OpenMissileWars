#!/bin/sh

set -e

./build.sh

if [ -e deployment ]; then
  echo "Cleaning previous deployment"
  rm -r deployment
fi

echo "Creating deployment"
mkdir -p deployment
cd deployment

echo "Copying server"
cp -RL ../server server

echo "Creating tarball"
tar -cf server.tar server

echo "Gzipping tarball"
gzip server.tar

echo "Copying launcher"
cp ../launcher/target/openmissilewars-launcher-jar-with-dependencies.jar launcher.jar
