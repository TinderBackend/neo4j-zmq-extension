#!/bin/sh
rm -rf target/
mvn clean
MAVEN_OPTS="-Xmx256M -Xms256M -server -d64" mvn compile
MAVEN_OPTS="-Xmx256M -Xms256M -server -d64" mvn package