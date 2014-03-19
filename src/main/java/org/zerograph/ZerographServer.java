package org.zerograph;

/**
 * Created by gabriellipson on 3/7/14.
 */

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.kernel.impl.util.StringLogger;

import java.io.File;

import static org.neo4j.helpers.Settings.*;


public class ZerographServer {
    public static final String SERVICE_NAME = "ZEROGRAPH_SERVER";
    public GraphDatabaseService database;

    public ZerographServer(GraphDatabaseService db, StringLogger logger, HostnamePort hostnamePort, Integer numThreads) {
    }

    public static void main(final String[] args) throws Throwable {
        final File directory = new File(args[0]);
        boolean newDB=!directory.exists();
        System.out.println("Using database "+directory+" new "+newDB);
        final GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(args[0]).setConfig(setting("zerograph_enabled",BOOLEAN,"true"),"true").newGraphDatabase();
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