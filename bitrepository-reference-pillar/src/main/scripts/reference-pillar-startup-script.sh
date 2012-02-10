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
export CLASSPATH=lib/activemq-all-5.4.3.jar:lib/bitrepository-collection-settings-0.4.jar:lib/bitrepository-common-0.8-SNAPSHOT.jar:lib/bitrepository-common-0.8-SNAPSHOT-tests.jar:lib/bitrepository-protocol-0.8-SNAPSHOT.jar:lib/bitrepository-reference-pillar-0.8-SNAPSHOT.jar:lib/commons-io-2.0.1.jar:lib/commons-lang-2.6.jar:lib/dom4j-1.6.1.jar:lib/jaccept-core-0.2.jar:lib/jaxb2-basics-runtime-0.6.2.jar:lib/jentity-core-0.2.jar:lib/junit-3.8.1.jar:lib/log4j-1.2.14.jar:lib/logback-classic-0.9.29.jar:lib/logback-core-0.9.29.jar:lib/slf4j-api-1.6.2.jar;

# Launch the application
java -cp $CLASSPATH org.bitrepository.pillar.ReferencePillarLauncher . $PWD/conf
