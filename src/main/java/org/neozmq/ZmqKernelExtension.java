package org.neozmq;

import org.neo4j.kernel.lifecycle.Lifecycle;
import org.zeromq.ZMQ;

import java.util.ArrayList;

public class ZmqKernelExtension implements Lifecycle, Runnable {

    public final static String INTERNAL_ADDRESS = "inproc://workers";

    private final ZmqKernelExtensionFactory.Dependencies DEPS;
    private final Integer THREAD_CT;
    private final ZMQ.Context CONTEXT;
    private final ArrayList<Worker> WORKERS = new ArrayList<Worker>();
    private final String EXTERNAL_ADDRESS;
    private final Integer PORT;
    private final ZMQ.Socket external;
    private final ZMQ.Socket internal;

    public ZmqKernelExtension(ZmqKernelExtensionFactory.Dependencies deps) {
        this.DEPS = deps;
        this.THREAD_CT = DEPS.getConfig().get(ZmqKernelExtensionFactory.ZmqSettings.zmq_threads);
        this.PORT = DEPS.getConfig().get(ZmqKernelExtensionFactory.ZmqSettings.zmq_address).getPort();
        this.EXTERNAL_ADDRESS = "tcp://*:" + PORT;
        this.CONTEXT = ZMQ.context(1);
        this.external = CONTEXT.socket(ZMQ.ROUTER);
        this.internal = CONTEXT.socket(ZMQ.DEALER);
        this.internal.bind(INTERNAL_ADDRESS);
        this.external.bind(EXTERNAL_ADDRESS);
        System.out.println("ZMQ: CONSTRUCTOR CALLED");
    }
    @Override
    public synchronized void init() throws Throwable {
        System.out.println("ZMQ: INIT CALLED");
        new Thread(this).start();
    }

    @Override
    public void start() {
        System.out.println("ZMQ: START CALLED");
        startWorkers();
    }

    @Override
    public void stop() {
        System.out.println("ZMQ: STOP CALLED");
        stopWorkers();
    }

    @Override
    public void shutdown() {
        System.out.println("ZMQ: SHUTDOWN CALLED");
        internal.close();
        external.close();
        CONTEXT.term();
    }

    @Override
    public void run() {
        System.out.println("ZMQ: RUN CALLED");
        ZMQ.proxy(external, internal, null);
    }
    private synchronized void startWorkers (){
        Integer startedCt = 0;
        System.out.println("ZMQ: Starting "+ THREAD_CT +" workers on port " + PORT);
        while(WORKERS.size()<THREAD_CT) {
            Worker w = new Worker(DEPS,CONTEXT,INTERNAL_ADDRESS);
            WORKERS.add(w);
            startedCt++;
        }
        System.out.println("ZMQ: Started "+ startedCt +" workers on port " + PORT);
    }

    private synchronized void stopWorkers () {
        Integer stoppedCt = WORKERS.size();
        System.out.println("ZMQ: Stopping " + stoppedCt + " workers on port " + PORT);
        for (Worker w : WORKERS) {
            w.stop();
        }
        WORKERS.clear();
        System.out.println("ZMQ: Stopped " + stoppedCt + " workers on port " + PORT);

    }

}
