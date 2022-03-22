#!/bin/bash

###
# #%L
# Bitrepository Integration
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
#Script for initiating the quick start default deployment of  the Bitrepository

if [ -z "$JAVA_HOME" ]; then
	echo "JAVA_HOME has not been set, cannot setup quickstart"
	exit 1
fi

#Make sure we are in the scripts folder
cd $(dirname $(perl -e "use Cwd 'abs_path';print abs_path('$0');"))
#Go back to the quickstart "root dir"
quickstartDir=$(pwd)

#Find the configurations directory
cd conf/
configDir=$(pwd)
cd $quickstartDir

#Make a directory for log files
if [ ! -d "logs" ]; then 
	mkdir logs
fi

#Make a directory for file storage
if [ ! -d "var" ]; then
	mkdir var
fi
cd var 
if [ ! -d "checksumpillar" ]; then
	mkdir checksumpillar
fi
if [ ! -d "file1pillar" ]; then
	mkdir file1pillar
fi
if [ ! -d "file2pillar" ]; then
	mkdir file2pillar
fi
cd $quickstartDir

#Setup configuration files, this includes:
# - Create symlinks for RepositorySettings files in relevant configuration folders
# - Setup paths to configs in relevant config files 
#for directory in $(ls -l conf | grep "^d" | cut -d " " -f10) 
for directory in $(find conf -maxdepth 1 -mindepth 1 -type d)
do
        cd "$directory"
        if [ $(basename $directory) = "integrityservice" ]; then
          mkdir "reportdir"
        fi
        ln -sf ../RepositorySettings.xml .
        sed -i'' -e s%\<\!--foobarpattern--\>%$quickstartDir/% ReferenceSettings.xml > /dev/null
        sed -i'' -e s%\$\{user.home\}%$quickstartDir% logback.xml
        cd $quickstartDir
done

sed -i'' -e s%eventsfile%$quickstartDir/logs/webclient-events% $configDir/webclient/webclient.properties

for file in $(find tomcat-services -iname '*.xml')
do
	sed -i'' -e s%\${user.home}%$quickstartDir% $file
done

echo "Installing pillars"
refPillarTarFile=$(ls bitrepository-reference-pillar*.tar.gz)
if [ ! -z $refPillarTarFile ]; then
	tar xf $refPillarTarFile > /dev/null
	rm $refPillarTarFile
fi

echo "Installing 'checksumpillar'"
refPillarDistDir=$(ls -t | grep bitrepository-reference-pillar-.* | head -1)
if [ ! -d "checksumpillar" ]; then 
	mkdir "checksumpillar"
	cp -r $refPillarDistDir/lib checksumpillar/.
	cp -r $refPillarDistDir/bin checksumpillar/.
	ln -s ../conf/checksumpillar checksumpillar/conf
fi

echo "Installing 'file1pillar'"
if [ ! -d "file1pillar" ]; then
	mkdir "file1pillar" 
	cp -r $refPillarDistDir/lib file1pillar/.
	cp -r $refPillarDistDir/bin file1pillar/.
	ln -s ../conf/file1pillar file1pillar/conf
fi

echo "Installing 'file2pillar'"
if [ ! -d "file2pillar" ]; then
        mkdir "file2pillar"
        cp -r $refPillarDistDir/lib file2pillar/.
        cp -r $refPillarDistDir/bin file2pillar/.
        ln -s ../conf/file2pillar file2pillar/conf
fi

rm -r $refPillarDistDir

commandlineTarFile=$(ls bitrepository-client*.tar.gz)
if [ ! -z $commandlineTarFile ]; then
        tar xf $commandlineTarFile > /dev/null
        rm $commandlineTarFile
fi

commandlineDistDir=$(ls -t | grep bitrepository-client* | head -1)
if [ ! -d "commandline" ]; then
    mv $commandlineDistDir "commandline"
    cd commandline
    rm -rf conf
    ln -s ../conf/commandline conf
    ln -s conf/logback.xml
fi

cd $quickstartDir

#Fetch, unpack, setup Apache Tomcat server
echo "Installing tomcat"
if [ -d "tomcat" ]; then
	rm -rf tomcat
fi
curl https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.59/bin/apache-tomcat-9.0.59.tar.gz > tomcat.tar.gz
tar xfz tomcat.tar.gz
mv apache-tomcat-* tomcat
rm tomcat.tar.gz

cd tomcat/conf
if [ ! -d Catalina ]; then
        echo "Creating Catalina dir"
        mkdir Catalina
        cd Catalina
        mkdir localhost
fi
cd $quickstartDir

for app in bitrepository-webclient.xml bitrepository-alarm-service.xml bitrepository-integrity-service.xml bitrepository-audit-trail-service.xml  bitrepository-monitoring-service.xml ; do
	ln -sf $quickstartDir/tomcat-services/$app $quickstartDir/tomcat/conf/Catalina/localhost/$app
done

${quickstartDir}/quickstart.sh start

echo "Bit repository GUI can now be accessed at http://localhost:8080/bitrepository-webclient"



