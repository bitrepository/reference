#!/bin/bash
# Precommit script for checking everything is ready for a commit together with update functionality for trivial stuff.

echo "Fixing svn properties on files"
svn propset svn:keywords "URL Revision Author Date Id" -R -q *

echo "Updating file headers"
mvn license:update-file-header -q

echo "Updating project license"
mvn license:update-project-license -q

echo "Updating third party licenses"
mvn license:add-third-party -q

echo "Running regression test"
mvn clean install -q