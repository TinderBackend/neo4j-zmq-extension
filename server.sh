#!/bin/sh
MAVEN_OPTS="-Xmx256M -Xms256M -server -d64" mvn compile exec:java -Djava.library.path=/usr/local/lib -Dexec.mainClass=org.neozmq.ZmqServer -Dexec.args=${1-graph.db}