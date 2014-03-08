package org.zerograph;

/**
 * Created by gabriellipson on 3/7/14.
 */

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.lifecycle.Lifecycle;

import java.io.File;
import java.util.ArrayList;


public class ZerographServer implements Lifecycle {
    public static final String SERVICE_NAME = "ZEROGRAPH_SERVER";
    final static private ArrayList<Thread> instances = new ArrayList<>();

    private final int numThreads;
    private final int port;
    private final StringLogger logger;
    private final String externalAddress;
    public GraphDatabaseService database;

    public ZerographServer(GraphDatabaseService db, StringLogger logger, HostnamePort hostnamePort, Integer numThreads) {
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
        final Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                db.shutdown();
            }
        });
        Service.WORKER_COUNT = 10;
        Service.DATABASE = db;
        Service.start(5555);
        mainThread.join();
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public synchronized void start() throws Throwable {
        Service.WORKER_COUNT = numThreads;
        Service.DATABASE = database;
        Service.start(port);
    }

    @Override
    public synchronized void stop() throws Throwable {
        Service.stop(port);

    }

    @Override
    public void shutdown() throws Throwable {
        stop();
    }


    public synchronized static boolean isRunning(int port) {
        return instances.size() > 0;
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