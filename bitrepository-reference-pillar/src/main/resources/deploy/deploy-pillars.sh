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

VERSION="$2"
DIR=$PWD
PILLAR_DIR="$DIR/pillars"
DOWNLOAD_DIR=$DIR/download
# Contains reference configuration, the specific configurations are pulling from.
# Changes here are pulled from the $DOWNLOAD_DIR configuration dir.
export STANDARD_CONFIG_DIR=$DIR/masterpillar
export DEPLOY_SCRIPTS=deploy/scripts
export ARTIFACTID="bitrepository-reference-pillar"

# Creates a dir for each specified on the commandline
create_root_dirs() {
    if [ ! -d "$DOWNLOAD_DIR" ]; then
       mkdir $DOWNLOAD_DIR
    fi
    if [ ! -d "$PILLAR_DIR" ]; then
      mkdir $PILLAR_DIR
    fi
}

create_conf_repos() {
    if [ ! -d "${DOWNLOAD_DIR}/.git" ] ; then
        ${DEPLOY_SCRIPTS}/gitutils.sh create $DOWNLOAD_DIR
    fi
    if [ ! -d "$STANDARD_CONFIG_DIR" ] ; then
      ${DEPLOY_SCRIPTS}/gitutils.sh create $STANDARD_CONFIG_DIR $DOWNLOAD_DIR
    fi
    # All arguments after the first 3 are considered pillar names.
    all=( ${@} )
    IFS=','
    pillars="${all[*]:2}"
    #for ((i = 3; i <= $#; i++))
    for var in $pillars
    do
      echo "Creating $var folder"
      ${DEPLOY_SCRIPTS}/gitutils.sh create $PILLAR_DIR/$var $STANDARD_CONFIG_DIR
    done

}

# Download the artifacts
download() {
  rm -r ${ARTIFACTID}-*
  echo "Downloading new deployment scripts"
  #${DEPLOY_SCRIPTS}/nxfetch.sh -i org.bitrepository.reference:$ARTIFACTID:"$VERSION" -c deploy -p tar.gz
  #tar -xzf $ARTIFACTID-deploy.tar.gz -C ../
  echo "Downloading new pillar artifacts"
  ${DEPLOY_SCRIPTS}/nxfetch.sh -i org.bitrepository.reference:$ARTIFACTID:"$VERSION" -c distribution -p tar.gz
  tar -xzf $ARTIFACTID-distribution.tar.gz
  rm -rf $DOWNLOAD_DIR/lib
  cp -R ${ARTIFACTID}*/* $DOWNLOAD_DIR
}

# Updates the pillar
update_pillars() {
  echo "Committing downloaded changes"
  ${DEPLOY_SCRIPTS}/gitutils.sh commit_all $DOWNLOAD_DIR "Committing downloaded changes"
  echo "Pulling to master"
  ${DEPLOY_SCRIPTS}/gitutils.sh pull $STANDARD_CONFIG_DIR
  if [ ! -d "$STANDARD_CONFIG_DIR/conf" ] ; then
     mkdir "$STANDARD_CONFIG_DIR/conf"
  fi
  if [ -e "${DEPLOY_SCRIPTS}/fetchconf.sh" ] ; then
    echo "Updating master conf"
    ${DEPLOY_SCRIPTS}/fetchconf.sh ${STANDARD_CONFIG_DIR}/conf
    ${DEPLOY_SCRIPTS}/gitutils.sh commit_all ${STANDARD_CONFIG_DIR} "Fetched new conf files"
  fi
  for i in ${PILLAR_DIR}/*
   do
     if [ -d $i ]
     then
       echo "Updating pillar " $i
       cd $i
       if [ -d lib ]; then
         rm lib
       fi
       ln -s $DOWNLOAD_DIR/lib lib
       cd ../..
       ${DEPLOY_SCRIPTS}/gitutils.sh pull $i
     fi
   done
}

# Cleans the pillar
clean_pillars() {
  for i in ${PILLAR_DIR}/*
   do
     if [ -d $i ]
     then
       echo "Cleaning pillar " $i
       cd $i
       if [ -d databases ]; then
         rm -r databases
       fi
       rm -f *.log
       rm -f *.out
       bin/create_derby_databases.sh
     fi
   done
}

set -e
case "$1" in
  create)
    create_root_dirs
    download
    create_conf_repos ${@}
    update_pillars
    clean_pillars
	case "$?" in
		0) echo "$NAME Pillars have now been created under $PILLAR_DIR." ;;
		*) echo "$NAME failed to create pillars" ;;
	esac
	;;
  update)
    download
	update_pillars
	case "$?" in
		0) echo "$NAME has been updated" ;;
		1) echo "$NAME failed to update" ;;
	esac
	;;
	clean)
    	clean_pillars
    ;;
  *)
	echo "Usage: $SCRIPTNAME {create|update|clean}" >&2
	exit 4
	;;
esac