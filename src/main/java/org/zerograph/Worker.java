package org.zerograph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.zerograph.except.ClientError;
import org.zerograph.resources.*;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.UUID;

public class Worker implements Runnable {

    final public static String ADDRESS = "inproc://workers";

    final private UUID uuid;
    final private Service service;
    final private GraphDatabaseService database;
    final private ZMQ.Socket external;

    final private CypherResource cypherResource;
    final private DatabaseResource databaseResource;
    final private NodeResource nodeResource;
    final private NodeSetResource nodeSetResource;
    final private RelResource relResource;

    public Worker(Service service) {
        this.uuid = UUID.randomUUID();
        this.service = service;
        this.database = service.getDatabase();
        this.external = service.getContext().socket(ZMQ.REP);

        this.cypherResource = new CypherResource(this.database, this.external);
        this.databaseResource = new DatabaseResource(this.database, this.external);
        this.nodeResource = new NodeResource(this.database, this.external);
        this.nodeSetResource = new NodeSetResource(this.database, this.external);
        this.relResource = new RelResource(this.database, this.external);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            this.external.connect(ADDRESS);
            ArrayList<Request> requests = new ArrayList<>();
            // parse requests
            try {
                boolean more = true;
                while (more) {
                    String frame = external.recvStr();
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
            }
            // handle requests
            ArrayList<PropertyContainer> outputValues = new ArrayList<>(requests.size());
            try {
                System.out.println("--- Beginning transaction in worker " + this.uuid.toString() + " ---");
                try (Transaction tx = database.beginTx()) {
                    for (Request request : requests) {
                        request.resolvePointers(outputValues);
                        switch (request.getResource()) {
                            case CypherResource.NAME:
                                outputValues.add(cypherResource.handle(tx, request));
                                break;
                            case DatabaseResource.NAME:
                                outputValues.add(databaseResource.handle(tx, request));
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
                System.out.println("--- Successfully completed transaction in worker " + this.uuid.toString() + " ---");
            } catch (IllegalArgumentException ex) {
                send(new Response(Response.BAD_REQUEST, ex.getMessage()));
            } catch (TransactionFailureException ex) {
                send(new Response(Response.CONFLICT, ex.getMessage()));  // TODO - derive cause from nested Exceptions
            } catch (ClientError ex) {
                send(ex.getResponse());
            } catch (Exception ex) {
                send(new Response(Response.SERVER_ERROR, ex.getMessage()));
            } finally {
                System.out.println();
            }
        }
    }

    public boolean send(Response response) {
        String string = response.toString();
        //System.out.println(">>> " + string);
        return external.send(string);
    }

}
