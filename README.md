# Zerograph-Extension

## KernelExtension

To run as a kernel extension, package and add to /plugins

```bash
$ cd neo4j_zerograph_extension
$ mvn clean package
$ cp target/neo4j_zerograph_extension-1.0-SNAPSHOT-jar-with-dependencies.jar $NEO4J_HOME/plugins/zg_extension.jar
$ neo4j restart
```
## Server

To run the server standalone (not as an extension):

```bash
$ ./server.sh
```

## Client

To run the Python test shell:

```bash
$ pip3 install pyzmq
$ cd python3-client
$ python3 -m zerograph.shell
```
