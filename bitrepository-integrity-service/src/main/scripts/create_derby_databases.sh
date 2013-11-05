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

CONFDIR="../../src/test/conf"
FINAL_DIR="../.."
CLASSPATH="-classpath ./:WEB-INF/lib/*:WEB-INF/classes"
INTEGRITY_DB_SCRIPT="sql/derby/integrityDBCreation.sql";
AUDIT_CONTRIBUTOR_DB_SCRIPT="sql/derby/auditContributorDBCreation.sql";
JAVA="/usr/bin/java"

echo "Running integrity service database creation from $PWD dir"
echo "Unzipping war to get access to lib files."
unzip -o -qq *.war

echo "Creating integrity database."
$JAVA $CLASSPATH org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator $CONFDIR $INTEGRITY_DB_SCRIPT
echo "Creating audit database."
$JAVA $CLASSPATH org.bitrepository.integrityservice.audittrail.IntegrityAuditTrailDatabaseCreator $CONFDIR $AUDIT_CONTRIBUTOR_DB_SCRIPT

cp -r target $FINAL_DIR
rm -r target