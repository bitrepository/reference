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
quickstartdir=$(pwd)
export CATALINA_PID="${quickstartdir}/tomcat/pid.tomcat"

#
# Function that starts the quickstart components
#
do_start() {
    export CATALINA_OPTS="-Xms256m -Xmx1028m -XX:PermSize=256m -XX:MaxPermSize=256m"
    ${quickstartdir}/reference1pillar/bin/reference-pillar.sh start
    ${quickstartdir}/reference2pillar/bin/reference-pillar.sh start
    ${quickstartdir}/checksumpillar/bin/checksum-pillar.sh start
    ${quickstartdir}/tomcat/bin/catalina.sh start
}

#
# Function that stops the quickstart components
#
do_stop() {
    ${quickstartdir}/tomcat/bin/catalina.sh stop -force
    ${quickstartdir}/reference1pillar/bin/reference-pillar.sh stop
    ${quickstartdir}/reference2pillar/bin/reference-pillar.sh stop
    ${quickstartdir}/checksumpillar/bin/checksum-pillar.sh stop
}

#
# Function that lists the status of the quickstart components
#
do_status() {
    ${quickstartdir}/reference1pillar/bin/reference-pillar.sh status
    ${quickstartdir}/reference2pillar/bin/reference-pillar.sh status
    ${quickstartdir}/checksumpillar/bin/checksum-pillar.sh status
    ${quickstartdir}/tomcat/bin/catalina.sh status
}

case "$1" in
  start)
	do_start
	;;
  stop)
	do_stop
	;;
  status)
	do_status
	;;
  restart|force-reload)
	do_stop
    do_start
	;;
  *)
	echo "Usage: $SCRIPTNAME {start|stop|status|restart}" >&2
	exit 4
	;;
esac




