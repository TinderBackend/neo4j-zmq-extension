package org.zerograph;

/**
 * Created by gabriellipson on 3/7/14.
 */

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.kernel.impl.util.StringLogger;

import java.io.File;


public class ZerographServer {
    public static final String SERVICE_NAME = "ZEROGRAPH_SERVER";
    private final int numThreads;
    private final int port;
    private final StringLogger logger;
    private final String externalAddress;
    public GraphDatabaseService database;

    public ZerographServer(GraphDatabaseService db, StringLogger logger, HostnamePort hostnamePort, Integer numThreads) {
        System.out.println("ZG constructor");
        this.logger = logger;
        externalAddress = "tcp://" + hostnamePort.getHost("*")+":"+hostnamePort.getPort();
        this.numThreads=numThreads;
        this.database = db;
        this.port = hostnamePort.getPort();
    }

    public static void main(final String[] args) throws Throwable {
        final File directory = new File(args[0]);
        boolean newDB=!directory.exists();
        System.out.println("Using database "+directory+" new "+newDB);
        final GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase( args[0] );
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                db.shutdown();
            }
        });
    }


}


/*
*
*
*
*
*
*
*
*
*
* */