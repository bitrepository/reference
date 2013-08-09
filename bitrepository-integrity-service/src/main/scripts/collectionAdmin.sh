#!/bin/bash

cd $(dirname $(readlink -f $0))

CLASSPATH="-classpath ../:../WEB-INF/lib/*:../WEB-INF/classes"
JAVA="/usr/bin/java"

$JAVA $CLASSPATH org.bitrepository.integrityservice.tools.CollectionsAdminLauncher "$@"
