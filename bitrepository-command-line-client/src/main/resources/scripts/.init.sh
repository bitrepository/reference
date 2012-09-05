#!/bin/bash

cd $(dirname $(readlink -f $0))

if [ ! -e conf ]; then
  echo "Requires the directory 'conf' with settings, etc. in folder `pwd`";
  echo "The directory could be a link to another directory, e.g. 'ln -s path/to/other/conf'";
  exit -1;
fi

CONFDIR="conf"
KEYFILE="conf/client-01.pem"
JAVA="/usr/bin/java"
JAVA_OPTS="-classpath conf:lib/*"