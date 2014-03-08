package org.zerograph.resources;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.Service;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;
import org.zerograph.except.ServiceAlreadyRunningException;
import org.zerograph.except.ServiceNotRunningException;
import org.zeromq.ZMQ;

public class DatabaseResource extends Resource {

    final public static String NAME = "db";
    public DatabaseResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * GET db {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer get(Transaction tx, Request request) throws ClientError, ServerError {
        int port = request.getIntegerData(0);
        if (Service.isRunning(port)) {
            sendOK();
        } else {
            throw new ClientError(new Response(Response.NOT_FOUND, "No database listening on port " + port));
        }
        return null;
    }

    /**
     * PUT db {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer put(Transaction tx, Request request) throws ClientError, ServerError {
        int port = request.getIntegerData(0);
        try {
            Service.start(port);
            sendCreated();
        } catch (ServiceAlreadyRunningException ex) {
            sendOK();
        }
        return null;
    }

    /**
     * DELETE db {port}
     *
     * @param request
     */
    @Override
    public PropertyContainer delete(Transaction tx, Request request) throws ClientError, ServerError {
        int port = request.getIntegerData(0);
        try {
            Service.stop(port);
            sendOK();
        } catch (ServiceNotRunningException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "No database listening on port " + port));
        }
        return null;
    }

}
