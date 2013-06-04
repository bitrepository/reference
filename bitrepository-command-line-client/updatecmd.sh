#! /bin/bash

mvn package
cp target/bitrepository-command-line-0.24-SNAPSHOT-distribution.tar.gz ~/products/
cd ~/products/
tar -xf bitrepository-command-line-0.24-SNAPSHOT-distribution.tar.gz
cd -
