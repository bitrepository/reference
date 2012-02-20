###
# #%L
# Bitrepository Reference Pillar
# 
# $Id$
# $HeadURL$
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
#!/bin/sh

# Asserting the script has been called from the bin directory
cd ..

# Export the variables, classpaths and dependencies.
set PWD=´pwd´

# check whether any processes already are running.
PIDS=$(ps -wwfe | grep org.bitrepository.pillar.ReferencePillarLauncher | grep -v grep | grep $PWD/conf | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    echo Application already running.
    exit -1;
fi;

export LOGBACK="-Dlogback.configurationFile=conf/logback.xml"
export CLASSPATH=`echo \`ls lib/*\` | sed s/' '/:/g`

export SSL_DEPENDENCIES="-Djavax.net.ssl.keyStore=conf/key.store -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=conf/trust.store -Djavax.net.ssl.trustStorePassword=123456"

# Launch the application
echo java -cp $CLASSPATH $LOGBACK $SSL_DEPENDENCIES org.bitrepository.pillar.ReferencePillarLauncher $PWD/conf
java -cp $CLASSPATH $LOGBACK $SSL_DEPENDENCIES org.bitrepository.pillar.ReferencePillarLauncher $PWD/conf > ReferencePillar.start 2>&1 &
