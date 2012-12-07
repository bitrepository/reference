#!/bin/bash

DIR=$PWD
PILLAR_DIR="$DIR/pillars"

# Remember to delete the irrelevant *-pillar.sh file in the
# pillars bin folder and rename the other to pillar.sh
for i in ${PILLAR_DIR}/*
do
  if [ -d $i ]
  then
    cd $i
    bin/pillar.sh $1
    cd ..
  fi
done