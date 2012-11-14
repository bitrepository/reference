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

# Create a new repository and populate it
initialize_repository() {
  if [ -z "$2" ] ; then
    echo "Initializing repository for $1"
    cd $1
    git init
    git add testprops/*
    git add -f bin/*
    echo lib >> .gitignore
    git add .gitignore
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
  git add testprops/*
  git add -f bin/*
  if [ $(git status --porcelain| wc -l) -gt 0 ] ; then
    echo "Committing changed files at $PWD: $(git status --porcelain)"
    git commit -m "New download $2" --quiet
  fi
}

# Commit all files to the repository
do_commit_all() {
  cd $1
  git add -f *
  if [ $(git status --porcelain| wc -l) -gt 0 ] ; then
    echo "Committing all files at $PWD: $(git status --porcelain)"
    git commit -m "$2" --quiet
    else  echo "No changes to $1 ($(git status --porcelain| wc -l)))"
  fi
}

# Commit any changes to the repository
do_pull() {
  cd $1
    echo "Pulling from $PWD"
    git fetch
    git pull
}

case "$1" in
  create)
    initialize_repository $2 $3
    ;;
  commit)
    do_commit $2 $3
    ;;
   commit_all)
     do_commit_all $2 $3
     ;;
  pull)
    do_pull $2 $3
    ;;
  *)
	echo "Usage: $SCRIPTNAME {create}" >&2
	exit 4
	;;
esac