package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.zerograph.except.ServiceAlreadyRunningException;
import org.zerograph.except.ServiceNotRunningException;
import org.zeromq.ZMQ;

import java.util.HashMap;

public class Service implements Lifecycle, Runnable {

    final static private HashMap<Integer, Thread> instances = new HashMap<>(1);

    public static int WORKER_COUNT = 40;
    public static GraphDatabaseService DATABASE = null;
    final private int port;
    final private String address;

    final private ZMQ.Context context;

    private ZMQ.Socket external;  // incoming requests from clients
    private ZMQ.Socket internal;  // request forwarding to workers

    public Service(int port) {
        this.port = port;
        this.address = "tcp://*:" + port;
        this.context = ZMQ.context(1);
    }

    public ZMQ.Context getContext() {
        return this.context;
    }

    public GraphDatabaseService getDatabase() {
        return this.DATABASE;
    }

    public synchronized static boolean isRunning(int port) {
        return instances.containsKey(port);
    }

    public synchronized static Service start(int port ) throws ServiceAlreadyRunningException {

        if (instances.containsKey(port)) {
            throw new ServiceAlreadyRunningException(port);
        } else {
            Service service = new Service(port);
            Thread thread = new Thread(service);
            try {
                thread.start();
            } catch (Exception ex) {
                throw new ServiceAlreadyRunningException(port);
            }
            instances.put(port, thread);
            return service ;
        }
    }

    public synchronized static void stop(int port) throws ServiceNotRunningException {
        if (instances.containsKey(port)) {
            Thread thread = instances.get(port);
            // TODO: can't kill current db
            thread.interrupt();
        } else {
            throw new ServiceNotRunningException(port);
        }
        instances.remove(port);
    }

    private void bind() {
        this.external = context.socket(ZMQ.ROUTER);
        this.external.bind(address);
        this.internal = context.socket(ZMQ.DEALER);
        this.internal.bind(Worker.ADDRESS);
    }

    private void startWorkers(int count) {
        for(int i = 0; i < count; i++) {
            new Thread(new Worker(this)).start();
        }
    }

    public void run() {
        System.out.println("Starting up " + this.port + "(" + WORKER_COUNT + "threads)");
        bind();
        startWorkers(WORKER_COUNT);
        ZMQ.proxy(external, internal, null);
        System.out.println("Shutting down " + this.port + "(" + WORKER_COUNT + "threads)");
        external.close();
        internal.close();
        context.term();
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void start() throws Throwable {
        Service.start(port);
    }

    @Override
    public void stop() throws Throwable {
        Service.stop(port);
    }

    @Override
    public void shutdown() {
    }

}