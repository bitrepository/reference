#!/bin/bash

###
# #%L
# Bitrepository Reference Pillar
# %%
# Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
# %%
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 2.1 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Lesser Public License for more details.
#
# You should have received a copy of the GNU General Lesser Public
# License along with this program.  If not, see
# <http://www.gnu.org/licenses/lgpl-2.1.html>.
# #L%
###

LOGBACK="-Dlogback.configurationFile=testprops/logback-integration-test.xml" #configuration directory
CONFDIR="conf"
KEYFILE="conf/client-01.pem" #key file
JAVA="java"
TEST_FILE="testprops/full-pillar-test.xml"
JAVA_OPTS="-classpath ..:testprops/::lib/* org.testng.TestNG"
#DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=11111"

rm -r log
rm -r test-output
rm -r target
mkdir target

$JAVA $DEBUG_OPTS $LOGBACK $JAVA_OPTS $TEST_FILE

exit $?
