#!/bin/sh
MAVEN_OPTS="-Djava.library.path=/usr/local/lib -Xmx256M -Xms256M -server -d64" mvn compile exec:java -Dexec.mainClass=org.zerograph.ZerographServer -Dexec.args=${1-graph.db}