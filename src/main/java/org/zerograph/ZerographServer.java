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



public class ZerographServer implements Lifecycle {
    public static final String SERVICE_NAME = "ZEROGRAPH_SERVER";

    private static final String WORKER_ADDRESS = "inproc://workers";

    private final int numThreads;

    private final StringLogger logger;
    private final String externalAddress;

    public ZerographServer(GraphDatabaseService db, StringLogger logger, HostnamePort hostnamePort, Integer numThreads) {
        this.logger = logger;
        externalAddress = "tcp://" + hostnamePort.getHost("*")+":"+hostnamePort.getPort();
        this.numThreads=numThreads;
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
        mainThread.join();
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void start() throws Throwable {

    }

    @Override
    public void stop() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        stop();
    }

}