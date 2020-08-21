FROM openjdk:11-jre-slim

WORKDIR server

RUN apt-get update
RUN apt-get install -y curl

ARG VERSION

RUN curl -L --output server.tar.gz https://github.com/LlewVallis/OpenMissileWars/releases/download/$VERSION/server.tar.gz

RUN gzip -d server.tar.gz && \
  tar -xf server.tar && \
  rm server.tar && \
  mv server/* . && \
  rm -r server

ENTRYPOINT ./start.sh
