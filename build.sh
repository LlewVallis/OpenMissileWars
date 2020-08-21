#!/bin/sh

set -e

echo "Downloading artifacts"
cd artifacts
./setup.sh
cd ..

echo "Building plugin"
cd plugin
mvn package
cd ..

echo "Building agent"
cd agent
mvn package
cd ..
