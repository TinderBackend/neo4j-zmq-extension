package org.neozmq;

import org.neo4j.kernel.lifecycle.Lifecycle;
import org.zeromq.ZMQ;

import java.util.ArrayList;

public class ZmqLifecycle implements Lifecycle, Runnable {

    private ZmqKernelExtensionFactory.Dependencies DEPS = null;
    private Integer THREAD_CT = null;
    private ZMQ.Context CONTEXT = null;
    private ArrayList<Thread> THREADS = new ArrayList<>();
    private String EXTERNAL_ADDRESS = null;
    private Integer PORT = null;
    final public static String INTERNAL_ADDRESS = "inproc://workers";


    private ZMQ.Socket external = null;
    private ZMQ.Socket internal = null;

    public ZmqLifecycle(ZmqKernelExtensionFactory.Dependencies deps) {
        this.DEPS = deps;
        this.THREAD_CT = DEPS.getConfig().get(ZmqKernelExtensionFactory.ZmqSettings.zmq_threads);
        this.PORT = DEPS.getConfig().get(ZmqKernelExtensionFactory.ZmqSettings.zmq_address).getPort();
        this.EXTERNAL_ADDRESS = "tcp://*:" + PORT;
    }
    @Override
    public void init() throws Throwable {
    }

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void shutdown() {
    }

    @Override
    public void run() {
        this.CONTEXT = ZMQ.context(1);
        this.external = CONTEXT.socket(ZMQ.ROUTER);
        this.external.bind(EXTERNAL_ADDRESS);
        this.internal = CONTEXT.socket(ZMQ.DEALER);
        this.internal.bind(INTERNAL_ADDRESS);
        startWorkers();
        ZMQ.proxy(external, internal, null);
        external.close();
        internal.close();
        System.out.println("Stopping " + THREADS.size() + " zmq workers on port " + PORT);
        while(THREADS.size()>THREAD_CT) {
            Thread t = THREADS.get(0);
            t.interrupt();
            THREADS.remove(0);
        }
        CONTEXT.term();
    }
    public synchronized void startWorkers (){
        System.out.println("Starting "+ THREAD_CT +" zmq workers on port " + PORT);
        while(THREADS.size()<THREAD_CT) {
            Thread t = new Thread(new Worker(DEPS,CONTEXT,INTERNAL_ADDRESS));
            THREADS.add(t);
            t.start();
        }
        System.out.println("Started "+ THREAD_CT +" zmq workers on port " + PORT);
    }
}
