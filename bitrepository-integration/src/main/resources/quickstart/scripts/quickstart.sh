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

#Make sure we are in the scripts folder
cd $(dirname $(readlink -f $0))
#Go back to the quickstart "root dir"
cd ..
quickstartdir=$(pwd)

#Find the configration directory
cd conf/
configdir=$(pwd)
cd $quickstartdir

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
if [ ! -d "referencepillar" ]; then
	mkdir referencepillar
fi
cd $quickstartdir

#Setup configuration files, this includes:
# - Create symlinks for CollectionSettings files in relevant configuration folders
# - Setup paths to configs in relevant config files 
for directory in $(ls -l conf | grep "^d" | cut -d " " -f9) 
do
        cd "conf/$directory"
	if [ -a "CollectionSettings.xml" ]; then
		rm CollectionSettings.xml
	fi
        ln -s ../CollectionSettings.xml .
	sed -i s%\<\!--foobarpattern--\>%$quickstartdir/% ReferenceSettings.xml > /dev/null
	sed -i s%\{user.home\}%$quickstartdir% logback.xml 
        cd $quickstartdir
done

sed -i s%eventsfile%$quickstartdir/logs/webclient-events% $configdir/webclient/webclient.properties 

for file in $(ls -l tomcat-services/*.xml | cut -d " " -f9) 
do
	sed -i s%\${user.home}%$quickstartdir% $file
done

refpillarzipfile=$(ls -l *.zip | cut -d " " -f9)
if [ ! -z $refpillarzipfile ]; then
	unzip $refpillarzipfile > /dev/null
	rm $refpillarzipfile
fi

refpillardistdir=$(ls | grep bitrepository-reference-pillar-*)

if [ ! -d "checksumpillar" ]; then 
	mkdir "checksumpillar"
	cp -r $refpillardistdir/lib checksumpillar/.
	cp -r $refpillardistdir/bin checksumpillar/.
	ln -s ../conf/checksumpillar checksumpillar/conf
	cd checksumpillar/bin
	./checksum-pillar-startup-script.sh
	cd $quickstartdir
fi

if [ ! -d "referencepillar" ]; then
	mkdir "referencepillar" 
	cp -r $refpillardistdir/lib referencepillar/.
	cp -r $refpillardistdir/bin referencepillar/.
	ln -s ../conf/referencepillar referencepillar/conf
	cd referencepillar/bin
	./reference-pillar-startup-script.sh
	cd $quickstartdir
fi

#Fetch, unpack, setup Apache Tomcat server
if [ -d "tomcat" ]; then
	rm -rf tomcat
fi
curl http://apache.mirrors.webname.dk/tomcat/tomcat-6/v6.0.35/bin/apache-tomcat-6.0.35.tar.gz > tomcat.tar.gz
tar xfz tomcat.tar.gz
mv apache-tomcat-* tomcat

cd tomcat/conf
if [ ! -d Catalina ]; then
        echo "Catalina dir is missing"
        mkdir Catalina
        cd Catalina
        mkdir localhost
fi
cd $quickstartdir

for app in bitrepository-webclient.xml bitrepository-alarm-service.xml bitrepository-integrity-service.xml bitrepository-audit-trail-service.xml  bitrepository-monitoring-service.xml ; do
	ln -sf $quickstartdir/tomcat-services/$app $quickstartdir/tomcat/conf/Catalina/localhost/$app
done

./tomcat/bin/catalina.sh start



