FROM openjdk:11-jre-slim

WORKDIR server

RUN apt-get update
RUN apt-get install -y curl

RUN curl -L --output server.tar.gz https://github.com/LlewVallis/OpenMissileWars/releases/latest/download/server.tar.gz

RUN gzip -d server.tar.gz && \
  tar -xf server.tar && \
  rm server.tar && \
  mv server/* . && \
  rm -r server

ENTRYPOINT ./start.sh
