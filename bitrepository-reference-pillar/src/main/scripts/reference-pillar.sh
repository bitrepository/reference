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

LOGBACK="-Dlogback.configurationFile=conf/logback.xml" #configuration directory
CONFDIR="conf"
KEYFILE="conf/client-01.pem" #key file
JAVA="/usr/bin/java"
JAVA_OPTS="-classpath conf:lib/* org.bitrepository.pillar.PillarLauncher"
PIDFILE="pillar.pid"
SCRIPTNAME="pillar"
NAME="pillar"

cd $(dirname $(perl -e "use Cwd 'abs_path';print abs_path('$0');"))/..
#Check availability of crucial system components
[ -x "$JAVA" ] || exit 2


#
# Function that starts the daemon/service
#
do_start() {
	# Return
	#   0 if daemon has been started
	#   1 if daemon was already running
	#   2 if daemon could not be started
	#Check to see if our deamon is already running
	if [ -e $PIDFILE ]
	then	
		kill -0 `cat $PIDFILE` 2>/dev/null #Check if ware indeed running
		case "$?" in 
			0) return 1 ;; #Process was already running
			*) rm $PIDFILE  # PIDFILE was left by previous instance	
		esac
	fi

	#Start our deamon	
	exec $JAVA $LOGBACK $JAVA_OPTS $CONFDIR $KEYFILE </dev/null >refpillar.out 2>&1 &
	echo $! > $PIDFILE
	sleep 1
	if [ -e $PIDFILE ]
	then
		return 0
	else
		return 2
	fi
}


#
# Function that stops the daemon/service
#
do_stop() {	
	# Return
	#   0 if daemon has been stopped
	#   1 if daemon was already stopped
	#   2 if daemon could not be stopped
	#   other if a failure occurred
	if [ -e $PIDFILE ]
	then	#PIDFILE exists kill our deamon
		kill `cat $PIDFILE`
	else	#PIDFILE did not exits, our deamon was not running
		return 1
	fi
	
	sleep 1
	kill -0 `cat $PIDFILE` 2>/dev/null 
	case "$?" in 
		0) return 2 ;; # Still running :(
		*) rm $PIDFILE && return 0 ;; #Daemon stoped, so remove PIDFILE and return
	esac
}


do_reload() {
	do_stop
	do_start
}

case "$1" in
  start)
	do_start
	case "$?" in
		0) echo "$NAME started" ;;
		1) echo "$NAME was already running" ;;
		2) echo "$NAME failed to start" ;;
	esac
	;;
  stop)
	do_stop
	case "$?" in
		0) echo "$NAME has been stopped" ;;
		1) echo "$NAME was not running" ;;
		2) echo "$NAME failed to stop" ;;
	esac
	;;
  status)
	if [ -e "$PIDFILE" ]
	then
		kill -0 `cat $PIDFILE` 2>/dev/null 
		case "$?" in 
			0) echo "$NAME is running" ;;
			*) echo "$NAME is not running" 
		esac
		
	else
		echo "$NAME is not running"
	fi
       ;;
  restart|force-reload)
	#
	# If the "reload" option is implemented then remove the
	# 'force-reload' alias
	#
	echo "Stopping $NAME"
	do_stop
	sleep 1 #Will fail a restart otherwise..
	case "$?" in
	  0|1)
		echo "Starting $NAME"
		do_start
		case "$?" in 
			0) echo "$NAME started" ;;
			2) echo "$NAME failed to start"
		esac
		;;
	  *)
		echo "$NAME failed to stop"
		;;
	esac
	;;
  *)
	echo "Usage: $SCRIPTNAME {start|stop|status|restart|force-reload}" >&2
	exit 4
	;;
esac

:


