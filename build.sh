#!/bin/sh

set -e

echo "Downloading artifacts"
cd artifacts
./setup.sh
cd ..

echo "Building plugin"
cd plugin
mvn package -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
cd ..

echo "Building agent"
cd agent
mvn package -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
cd ..

echo "Building launcher"
cd launcher
mvn package -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
cd ..
