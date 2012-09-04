#!/bin/bash

source .init.sh
GET_CHECKSUMS="org.bitrepository.commandline.GetChecksums"

exec $JAVA $JAVA_OPTS $GET_CHECKSUMS -s$CONFDIR -k$KEYFILE $* 


