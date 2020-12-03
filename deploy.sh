#!/bin/sh

set -e

python3 -V > /dev/null || exit 1

if [ -z "$GITHUB_TOKEN" ]; then
  echo GITHUB_TOKEN must be set
  exit 1
fi

./create-deployment.sh

cd uploader

if [ ! -d venv ]; then
  echo Creating Python virtual env
  python3 -m venv env
fi

echo Activating Python virtual env
source env/bin/activate

echo Installing dependencies
pip3 install -r requirements.txt

cd ../deployment

echo Running uploader
python3 ../uploader/uploader.py
