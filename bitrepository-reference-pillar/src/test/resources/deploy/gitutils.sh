#!/bin/bash

# Create a new repository and populate it
initialize_repository() {
  if [ -z "$2" ] ; then
    echo "Initializing repository for $1"
    cd $1
    git init
    git add testprops/*
    git add deploy/*
    git add -f bin/*
    git commit -m "Initialized with configurations" --quiet
  else
    if [ ! -d "$1/bin" ] ; then
      echo "Cloning $2 into $1"
      git clone --no-hardlinks $2 $1
      cd $1
    fi
  fi
}

# Commit any changes to the repository
do_commit() {
  cd $1
  if ! git diff --no-ext-diff --quiet; then
    git commit -m "New download $2" --quiet
  fi
}

# Commit any changes to the repository
do_pull() {
  cd $1
    echo "Pulling from $PWD"
    git pull
}

case "$1" in
  create)
    initialize_repository $2 $3
    ;;
  commit)
    do_commit $2 $3
    ;;
  pull)
    do_pull $2 $3
    ;;
  *)
	echo "Usage: $SCRIPTNAME {create}" >&2
	exit 4
	;;
esac