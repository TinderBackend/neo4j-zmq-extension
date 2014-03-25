package org.neozmq.resources;

import org.neo4j.graphdb.*;
import org.neozmq.Request;
import org.neozmq.Response;
import org.neozmq.except.ClientError;
import org.neozmq.except.ServerError;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class RelResource extends PropertyContainerResource {

    final public static String NAME = "rel";

    final private HashMap<String, RelationshipType> relationshipTypes;

    public RelResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
        this.relationshipTypes = new HashMap<>();
    }

    /**
     * GET rel {rel_id}
     *
     * Fetch a single relationship by ID.
     */
    @Override
    public PropertyContainer get(Transaction tx, Request request) throws ClientError, ServerError {
        long relID = request.getIntegerData(0);
        try {
            Relationship rel = database().getRelationshipById(relID);
            sendOK(rel);
            return rel;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Relationship " + relID + " not found"));
        }
    }

    /**
     * PUT rel {rel_id} {properties}
     *
     * Replace all properties on a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(Transaction tx, Request request) throws ClientError, ServerError {
        long relID = request.getIntegerData(0);
        Map properties = request.getMapData(1);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = tx.acquireWriteLock(rel);
            Lock readLock = tx.acquireReadLock(rel);
            removeProperties(rel);
            addProperties(rel, properties);
            readLock.release();
            writeLock.release();
            sendOK(rel);
            return rel;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Relationship " + relID + " not found"));
        }
    }

    /**
     * PATCH rel {rel_id} {properties}
     *
     * Add new properties to a relationship identified by ID.
     * This will not create a relationship with the given ID if one does not
     * already exist and any existing properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(Transaction tx, Request request) throws ClientError, ServerError {
        long relID = request.getIntegerData(0);
        Map properties = request.getMapData(1);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = tx.acquireWriteLock(rel);
            Lock readLock = tx.acquireReadLock(rel);
            addProperties(rel, properties);
            readLock.release();
            writeLock.release();
            sendOK(rel);
            return rel;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Relationship " + relID + " not found"));
        }
    }

    /**
     * POST rel {start_node} {end_node} {type} {properties}
     *
     * Create a new relationship.
     */
    @Override
    public PropertyContainer post(Transaction tx, Request request) throws ClientError, ServerError {
        Node startNode = resolveNode(request.getData(0));
        Node endNode = resolveNode(request.getData(1));
        String typeName = request.getStringData(2);
        Map properties = request.getMapData(3);
        Relationship rel = startNode.createRelationshipTo(endNode, getRelationshipType(typeName));
        Lock writeLock = tx.acquireWriteLock(rel);
        Lock readLock = tx.acquireReadLock(rel);
        addProperties(rel, properties);
        readLock.release();
        writeLock.release();
        sendCreated(rel);
        return rel;
    }

    /**
     * DELETE rel {rel_id}
     *
     * Delete a relationship identified by ID.
     */
    @Override
    public PropertyContainer delete(Transaction tx, Request request) throws ClientError, ServerError {
        long relID = request.getIntegerData(0);
        try {
            Relationship rel = database().getRelationshipById(relID);
            Lock writeLock = tx.acquireWriteLock(rel);
            rel.delete();
            writeLock.release();
            sendNoContent();
            return null;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Relationship " + relID + " not found"));
        }
    }

    private Node resolveNode(Object value) throws ClientError {
        if (value instanceof Node) {
            return (Node)value;
        } else if (value instanceof Integer) {
            try {
                return database().getNodeById((Integer)value);
            } catch (NotFoundException ex) {
                throw new ClientError(new Response(Response.NOT_FOUND, "Relationship " + value + " not found"));
            }
        } else {
            throw new ClientError(new Response(Response.BAD_REQUEST, value));
        }
    }

    private RelationshipType getRelationshipType(String name) {
        if (relationshipTypes.containsKey(name)) {
            return relationshipTypes.get(name);
        } else {
            DynamicRelationshipType relationshipType = DynamicRelationshipType.withName(name);
            relationshipTypes.put(name, relationshipType);
            return relationshipType;
        }
    }

}
