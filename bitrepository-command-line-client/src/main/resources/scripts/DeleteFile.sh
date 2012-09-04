#!/bin/bash

source .init.sh
DELETE_FILE="org.bitrepository.commandline.DeleteFile"

exec $JAVA $JAVA_OPTS $DELETE_FILE -s$CONFDIR -k$KEYFILE $* 

