#!/bin/sh

# Asserting the script has been called from the bin directory
cd ..
set PWD=´pwd´

# Find Process ID(s) and terminate is (them).
PIDS=$(ps -wwfe | grep org.bitrepository.pillar.ReferencePillarLauncher | grep -v grep | grep $PWD/conf | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    kill $PIDS;
fi
