#!/bin/bash

echo "Starting StorytimeLX"

#HEADLESS="--headless"
HEADLESS=""

if [[ $DISPLAY == "" ]]; then
  echo "Setting display"
  export DISPLAY=:0.0
fi

java -cp 'target/lxstudio-ide-0.4.1-jar-with-dependencies.jar:lib/processing-4.0b8/*' -Djava.library.path=lib/processing-4.0b8/native heronarts.lx.app.LXStudioApp $HEADLESS