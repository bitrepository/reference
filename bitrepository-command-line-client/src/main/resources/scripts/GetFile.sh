#!/bin/bash

source .init.sh
GET_FILE="org.bitrepository.commandline.GetFile"

exec $JAVA $JAVA_OPTS $GET_FILE -s$CONFDIR -k$KEYFILE $* 


