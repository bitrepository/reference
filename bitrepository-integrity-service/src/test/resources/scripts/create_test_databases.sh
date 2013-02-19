#!/bin/bash

# Creates the component-test database. Requires that the package phase has finished as the war files content are used.

cd target
tar -xf *-distribution.tar.gz
cd ${artifactId}-${version}
bin/create_derby_databases.sh
