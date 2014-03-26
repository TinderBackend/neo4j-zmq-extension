#!/bin/sh
rm -rf target/
MAVEN_OPTS="-Xmx256M -Xms256M -server -d64" mvn clean compile package -Djava.library.path=/usr/local/lib -Dexec.mainClass=org.neozmq.ZmqKernelExtensionFactory