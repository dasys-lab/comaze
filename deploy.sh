#!/usr/bin/env bash
mvn clean package\
&& ssh comaze 'rm comaze-server/*' \
&& scp target/comaze*.jar comaze:comaze-server/ \
&& ssh comaze 'screen -XS comaze-server quit'; \
echo 'Waiting for server to shut down...'; \
ssh comaze 'while nc -z localhost 16216; do  sleep 0.1; done'; \
echo 'Restarting server...'; \
ssh comaze 'screen -dmS comaze-server java -jar comaze-server/*jar'
