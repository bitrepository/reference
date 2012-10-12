#!/bin/bash

if [ -z "$1" ]; then
	echo "No version supplied, usage: test-pillars.sh <version>"
	exit 1
fi

VERSION="$2"
# TEST_DIR="/home/bitmag/pillar-tests/tests"
TEST_DIR=$PWD
TEST_CASE_DIR="$TEST_DIR/tests"
DOWNLOAD_TEST_DIR=$TEST_DIR/downloadedtest
# Contains reference configuration, the specific configurations are pulling from.
# Changes here are pulled from the $DOWNLOAD_TEST_DIR configuration dir.
STANDARD_CONFIG_DIR=$TEST_DIR/mastertest
DEPLOY_SCRIPTS=deploy
ARTIFACTID="bitrepository-reference-pillar"
TEST_FILE="conf/pillar-test.xml"
JAVA_OPTS="-classpath .:lib/* org.testng.TestNG"

# Creates a dir for each specified on the commandline
create_root_dirs() {
    if [ ! -d "$DOWNLOAD_TEST_DIR" ]; then
       mkdir $DOWNLOAD_TEST_DIR
    fi
    if [ ! -d "$TEST_CASE_DIR" ]; then
      mkdir $TEST_CASE_DIR
    fi
}

create_conf_repos() {
    if [ ! -d "${DOWNLOAD_TEST_DIR}/.git" ] ; then
        ${DEPLOY_SCRIPTS}/gitutils.sh create $DOWNLOAD_TEST_DIR
    fi
    if [ ! -d "$STANDARD_CONFIG_DIR" ] ; then
      ${DEPLOY_SCRIPTS}/gitutils.sh create $STANDARD_CONFIG_DIR $DOWNLOAD_TEST_DIR
    fi
    # All arguments after the first 3 are considered pillars test names.
    all=( ${@} )
    IFS=','
    pillars="${all[*]:2}"
    #for ((i = 3; i <= $#; i++))
    for var in $pillars
    do
      echo "Creating $var folder"
      ${DEPLOY_SCRIPTS}/gitutils.sh create $TEST_CASE_DIR/$var $STANDARD_CONFIG_DIR
    done

}

# Download the newest test
download_test() {
  echo "Downloading new deployment scripts"
  ${DEPLOY_SCRIPTS}/nxfetch.sh -i org.bitrepository.reference:$ARTIFACTID:"$VERSION" -c pillar-test-deploy -p tar.gz
  tar -xzf $ARTIFACTID.tar.gz
  echo "Downloading new test suite"
  ${DEPLOY_SCRIPTS}/nxfetch.sh -i org.bitrepository.reference:$ARTIFACTID:"$VERSION" -c pillar-test -p tar.gz
  tar -xzf $ARTIFACTID.tar.gz
  rm -rf $DOWNLOAD_TEST_DIR/lib
  cp -r ${ARTIFACTID}-${VERSION}/* $DOWNLOAD_TEST_DIR
}

# Updates a test for a single pillar
update_tests() {
  echo "Updating tests:"
  ${DEPLOY_SCRIPTS}/gitutils.sh commit $DOWNLOAD_TEST_DIR ${VERSION}
  ${DEPLOY_SCRIPTS}/gitutils.sh pull $STANDARD_CONFIG_DIR
  for i in ${TEST_CASE_DIR}/*
   do
     if [ -d $i ]
     then
       echo "Updating test " $i
       cd $i
       echo "Updating $PWD"
       if [ -d lib ]; then
         rm lib
       fi
       ln -s $DOWNLOAD_TEST_DIR/lib lib
       cd ../..
       ${DEPLOY_SCRIPTS}/gitutils.sh pull $i
     fi
   done
}

set -e
case "$1" in
  create)
    create_root_dirs
    download_test
    create_conf_repos ${@}
    update_tests
	case "$?" in
		0) echo "$NAME Pillar tests have now been created under $TEST_CASE_DIR." ;;
		*) echo "$NAME failed to create tests" ;;
	esac
	;;
  update)
    download_test
	update_tests
	case "$?" in
		0) echo "$NAME has been updated" ;;
		1) echo "$NAME failed to update" ;;
	esac
	;;
  run)
    for i in ${TEST_CASE_DIR}/*
    do
      if [ -d $i ]
      then
         cd $i
         echo "Running $PWD tests"
         bin/runpillartest.sh
         cd ..
      fi
    done
  ;;
  *)
	echo "Usage: $SCRIPTNAME {create|update|run}" >&2
	exit 4
	;;
esac