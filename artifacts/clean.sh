#!/bin/sh
for dir in */; do
  find $dir -maxdepth 1 -type f -name "bin" -delete
  find $dir -maxdepth 1 -type f -name "url-cached" -delete
done
