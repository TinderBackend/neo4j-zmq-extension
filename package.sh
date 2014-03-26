#!/bin/sh
rm -rf target/
MAVEN_OPTS="-Xmx256M -Xms256M -server -d64" mvn compile package