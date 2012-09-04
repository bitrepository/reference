#!/bin/bash

source .init.sh
GET_FILE_IDS="org.bitrepository.commandline.GetFileIDs"

exec $JAVA $JAVA_OPTS $GET_FILE_IDS -s$CONFDIR -k$KEYFILE $* 

