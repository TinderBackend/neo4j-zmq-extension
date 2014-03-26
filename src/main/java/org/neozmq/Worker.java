package org.neozmq;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neozmq.except.ClientError;
import org.neozmq.resources.CypherResource;
import org.neozmq.resources.NodeResource;
import org.neozmq.resources.NodeSetResource;
import org.neozmq.resources.RelResource;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker implements Runnable {

    final private UUID uuid;
    final private GraphDatabaseService database;
    final private ZMQ.Socket external;

    final private CypherResource cypherResource;
    final private NodeResource nodeResource;
    final private NodeSetResource nodeSetResource;
    final private RelResource relResource;
    final private AtomicBoolean running = new AtomicBoolean(false);

    private ZMQ.Context context;
    private String address = null;
    private StringLogger logger = null;

    public Worker( ZmqKernelExtensionFactory.Dependencies deps, ZMQ.Context ctx, String addr) {

        this.uuid = UUID.randomUUID();
        this.database = deps.getGraphDatabaseService();
        this.logger = deps.getStringLogger();
        this.external = ctx.socket(ZMQ.REP);
        this.context = ctx;
        this.address = addr;
        this.cypherResource = new CypherResource(this.database, this.external);
        this.nodeResource = new NodeResource(this.database, this.external);
        this.nodeSetResource = new NodeSetResource(this.database, this.external);
        this.relResource = new RelResource(this.database, this.external);
        new Thread(this).start();
    }

    @Override
    public void run() {
        running.set(true);
        this.external.connect(address);
        while (running.get()) {
            ArrayList<Request> requests = new ArrayList<>();
            // parse requests
            try {
                boolean more = true;
                while (more) {
                    String frame = external.recvStr(Charset.defaultCharset());
                    for (String line : frame.split("\\r|\\n|\\r\\n")) {
                        if (line.length() > 0) {
                            //System.out.println("<<< " + line);
                            requests.add(new Request(line));
                        }
                    }
                    more = external.hasReceiveMore();
                }
            } catch (ClientError ex) {
                send(ex.getResponse());
                continue;
            } catch (ZMQException ex) {
                break;
            }
            // handle requests
            ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
            try {
                try (Transaction tx = database.beginTx()) {
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        switch (request.getResource()) {
                            case CypherResource.NAME:
                                outputValues.add(cypherResource.handle(tx, request));
                                break;
                            case NodeResource.NAME:
                                outputValues.add(nodeResource.handle(tx, request));
                                break;
                            case NodeSetResource.NAME:
                                outputValues.add(nodeSetResource.handle(tx, request));
                                break;
                            case RelResource.NAME:
                                outputValues.add(relResource.handle(tx, request));
                                break;
                            default:
                                throw new ClientError(new Response(Response.NOT_FOUND, request.getResource()));
                        }
                    }
                    tx.success();
                }
                send(new Response(Response.OK));
            } catch (IllegalArgumentException ex) {
                send(new Response(Response.BAD_REQUEST, ex.getMessage()));
                logger.info("--- Failed transaction in worker " + this.uuid.toString() + " ---");
                logger.info(Response.BAD_REQUEST + " " + ex.getMessage() );
            } catch (TransactionFailureException ex) {
                send(new Response(Response.CONFLICT, ex.getMessage()));
                logger.info("--- Failed transaction in worker " + this.uuid.toString() + " ---");
                logger.info(Response.CONFLICT + " " + ex.getMessage() );
            } catch (ClientError ex) {
                send(ex.getResponse());
                logger.info("--- Failed transaction in worker " + this.uuid.toString() + " ---");
                logger.info(Response.BAD_REQUEST + " " + ex.getMessage() );
            } catch (ZMQException ex) {
                break;
            } catch (Exception ex) {
                send(new Response(Response.SERVER_ERROR, ex.getMessage()));
                logger.info("--- Failed transaction in worker " + this.uuid.toString() + " ---");
                logger.info(Response.SERVER_ERROR+ " " + ex.getMessage() );
            } finally {
            }
        }
        this.external.close();
    }



    public boolean send(Response response) {
        String string = response.toString();
        //System.out.println(">>> " + string);
        return external.send(string);
    }

    public void stop () {
        if (running.get()) {
            running.set(false);
        }
    }

}
