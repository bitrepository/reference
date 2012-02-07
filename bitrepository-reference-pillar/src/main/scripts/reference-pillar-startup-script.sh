#!/bin/sh

# Asserting the script has been called from the bin directory
cd ..

# Export the variables, classpaths and dependencies.
set PWD=´pwd´
export CLASSPATH=lib/activemq-all-5.4.3.jar:lib/bitrepository-collection-settings-0.4.jar:lib/bitrepository-common-0.8-SNAPSHOT.jar:lib/bitrepository-common-0.8-SNAPSHOT-tests.jar:lib/bitrepository-protocol-0.8-SNAPSHOT.jar:lib/bitrepository-reference-pillar-0.8-SNAPSHOT.jar:lib/commons-io-2.0.1.jar:lib/commons-lang-2.6.jar:lib/dom4j-1.6.1.jar:lib/jaccept-core-0.2.jar:lib/jaxb2-basics-runtime-0.6.2.jar:lib/jentity-core-0.2.jar:lib/junit-3.8.1.jar:lib/log4j-1.2.14.jar:lib/logback-classic-0.9.29.jar:lib/logback-core-0.9.29.jar:lib/slf4j-api-1.6.2.jar;

# Launch the application
java -cp $CLASSPATH org.bitrepository.pillar.ReferencePillarLauncher . $PWD/conf
