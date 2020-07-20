#!/bin/sh

ALLOCATED_MEMORY=2G

if [ ! -z $PAPER_MEMORY ]; then
  ALLOCATED_MEMORY=$PAPER_MEMORY
else
  echo Starting server with $ALLOCATED_MEMORY of RAM, use the PAPER_MEMORY environment variable to use a custom amount
fi

if [ ! -f eula.txt ]; then
  echo You have not accepted the Minecraft End User License Agreement \(https://account.mojang.com/documents/minecraft_eula\)
  read -p "Would you like to accept it? (y/n) " response
  case $response in
    [Yy]* ) echo "eula=true" > eula.txt;;
    * ) exit 1;;
  esac
fi

if [ ! -z $JVM_DEBUG_PORT ]; then
  DEBUG_ARGUMENTS="-agentlib:jdwp=transport=dt_socket,server=y,address=$JVM_DEBUG_PORT,suspend=n"
fi

cp -r configs/* .

java \
  -javaagent:agent.jar \
  -Xms$ALLOCATED_MEMORY -Xmx$ALLOCATED_MEMORY -XX:+UseG1GC -XX:+ParallelRefProcEnabled \
  -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC \
  -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 \
  -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 \
  -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 \
  -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true \
  $DEBUG_ARGUMENTS \
  -jar paper.jar nogui
