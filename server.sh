#!/bin/sh
MAVEN_OPTS="-Xmx256M -Xms256M -server -d64" mvn compile exec:java -Dexec.mainClass=org.neozmq.ZmqServer -Dexec.args=${1-graph.db}