#!/bin/sh
for dir in */; do
  find $dir -maxdepth 1 -type f -name "*.jar" -delete
done
