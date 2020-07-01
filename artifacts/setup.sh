#!/bin/sh
for dir in */; do
  pushd $dir > /dev/null
  ./setup.sh
  popd > /dev/null
done
