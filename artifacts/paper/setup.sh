#!/bin/sh

MINECRAFT_VERSION=1.16
PAPER_VERSION=27
ARTIFACT_NAME=paperclip-$PAPER_VERSION.jar

if [ ! -f $ARTIFACT_NAME ]; then
  curl --output $ARTIFACT_NAME https://papermc.io/ci/job/Paper-$MINECRAFT_VERSION/$PAPER_VERSION/artifact/$ARTIFACT_NAME
fi
