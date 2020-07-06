#!/bin/sh

echo "Downloading artifacts"
cd artifacts
./setup.sh
cd ..

echo "Building plugin"
cd plugin
mvn package
cd ..

echo "Executing server"
cd server
./start.sh
