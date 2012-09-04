#!/bin/bash

source .init.sh
PUT_FILE="org.bitrepository.commandline.PutFile"

exec $JAVA $JAVA_OPTS $PUT_FILE -s$CONFDIR -k$KEYFILE $* 


