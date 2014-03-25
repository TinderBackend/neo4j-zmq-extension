# neo4j-zmq-extension

This project heavily draws from the wonderful work done by Nigel Small on ZeroGraph, and by Michael Hunger on cypher_remoting_experiments and cypher_websocket_endpoint. This is an initial implementation of ZeroMQ as an RPC transport to communicate with Neo4j (server or embedded).

## KernelExtension

To run as a kernel extension, package and add to /plugins

```bash
$ cd neo4j_zmq_extension
$ mvn clean package
$ cp target/neo4j_zmq_extension-1.0-SNAPSHOT-jar-with-dependencies.jar $NEO4J_HOME/plugins/zg_extension.jar
$ neo4j restart
```
## Server

To run the server standalone (not as an extension):

```bash
$ ./server.sh
```
