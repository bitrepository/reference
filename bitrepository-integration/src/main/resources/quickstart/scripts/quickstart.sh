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
cd $(dirname $(perl -e "use Cwd 'abs_path';print abs_path('$0');"))
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
if [ ! -d "reference1pillar" ]; then
	mkdir reference1pillar
fi
if [ ! -d "reference2pillar" ]; then
	mkdir reference2pillar
fi
cd $quickstartdir

#Setup configuration files, this includes:
# - Create symlinks for CollectionSettings files in relevant configuration folders
# - Setup paths to configs in relevant config files 
#for directory in $(ls -l conf | grep "^d" | cut -d " " -f10) 
for directory in $(find conf -maxdepth 1 -mindepth 1 -type d)
do
        cd "$directory"
        ln -sf ../RepositorySettings.xml .
        sed -i s%\<\!--foobarpattern--\>%$quickstartdir/% ReferenceSettings.xml > /dev/null
        sed -i s%\$\{user.home\}%$quickstartdir% logback.xml 
        cd $quickstartdir
done

sed -i s%eventsfile%$quickstartdir/logs/webclient-events% $configdir/webclient/webclient.properties 

for file in $(find tomcat-services -iname '*.xml')
do
	sed -i s%\${user.home}%$quickstartdir% $file
done

refpillartarfile=$(ls bitrepository-reference-pillar*.tar.gz)
if [ ! -z $refpillartarfile ]; then
	tar xvf $refpillartarfile > /dev/null
	rm $refpillartarfile
fi

refpillardistdir=$(ls -t | grep bitrepository-reference-pillar-.* | head -1)
if [ ! -d "checksumpillar" ]; then 
	mkdir "checksumpillar"
	cp -r $refpillardistdir/lib checksumpillar/.
	cp -r $refpillardistdir/bin checksumpillar/.
	ln -s ../conf/checksumpillar checksumpillar/conf
fi
cd checksumpillar/bin
./checksum-pillar.sh start
cd $quickstartdir

if [ ! -d "reference1pillar" ]; then
	mkdir "reference1pillar" 
	cp -r $refpillardistdir/lib reference1pillar/.
	cp -r $refpillardistdir/bin reference1pillar/.
	ln -s ../conf/reference1pillar reference1pillar/conf
fi
cd reference1pillar/bin
./reference-pillar.sh start
cd $quickstartdir

if [ ! -d "reference2pillar" ]; then
        mkdir "reference2pillar"
        cp -r $refpillardistdir/lib reference2pillar/.
        cp -r $refpillardistdir/bin reference2pillar/.
        ln -s ../conf/reference2pillar reference2pillar/conf
fi
cd reference2pillar/bin
./reference-pillar.sh start
cd $quickstartdir

commandlinetarfile=$(ls bitrepository-command-line-*.tar.gz)
if [ ! -z $commandlinetarfile ]; then
        tar xvf $commandlinetarfile > /dev/null
        rm $commandlinetarfile
fi

commandlinedistdir=$(ls -t | grep bitrepository-command-line-* | head -1)
if [ ! -d "commandline" ]; then
    mv $commandlinedistdir "commandline"
    cd commandline
    rm -rf conf
    ln -s ../conf/commandline conf
    ln -s conf/logback.xml
fi
cd $quickstartdir

#Fetch, unpack, setup Apache Tomcat server
if [ -d "tomcat" ]; then
	rm -rf tomcat
fi
curl http://ftp.download-by.net/apache/tomcat/tomcat-6/v6.0.37/bin/apache-tomcat-6.0.37.tar.gz > tomcat.tar.gz
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



