#!/bin/sh

# Asserting the script has been called from the bin directory
cd ..

export CLASSPATH=`echo \`ls lib/derby*\` | sed s/' '/:/g`

java -cp $CLASSPATH org.apache.derby.tools.ij < conf/auditContributerDB.sql