#!/bin/bash

cd $(dirname $(readlink -f $0))

if [ ! -e conf ]; then
  echo "Requires the directory 'conf' with settings, etc. in folder `pwd`";
  echo "The directory could be a link to another directory, e.g. 'ln -s path/to/other/conf'";
  exit -1;
fi

CONFDIR="conf"
KEYFILE="conf/client-01.pem" #key file
JAVA="/usr/bin/java"
JAVA_OPTS="-classpath conf:lib/*"
PUT_FILE="org.bitrepository.commandline.PutFile"

exec $JAVA $JAVA_OPTS $PUT_FILE -s$CONFDIR -k$KEYFILE $* 


#java -cp lib/activemq-core-5.6.0.jar:lib/bitrepository-access-client-0.19-SNAPSHOT.jar:lib/bitrepository-client-0.19-SNAPSHOT.jar:lib/bitrepository-collection-settings-0.9.jar:lib/bitrepository-command-line-0.19-SNAPSHOT.jar:lib/bitrepository-core-0.19-SNAPSHOT.jar:lib/bitrepository-message-xml-19.jar:lib/bitrepository-modifying-client-0.19-SNAPSHOT.jar:lib/commons-cli-20040117.000000.jar:lib/commons-io-2.4.jar:lib/commons-lang-2.6.jar:lib/jaxb2-basics-runtime-0.6.4.jar:lib/slf4j-api-1.6.5.jar:lib/bcmail-jdk16-1.46.jar:lib/bcprov-jdk16-1.46.jar:lib/geronimo-j2ee-management_1.1_spec-1.0.1.jar:lib/geronimo-jms_1.1_spec-1.1.1.jar org.bitrepository.commandline.PutFile -sconf -ktest.txt -ftest.txt -cd41d8cd98f00b204e9800998ecf8427e
