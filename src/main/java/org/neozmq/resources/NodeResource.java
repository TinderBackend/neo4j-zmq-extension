package org.neozmq.resources;

import org.neo4j.graphdb.*;
import org.neozmq.Request;
import org.neozmq.Response;
import org.neozmq.except.ClientError;
import org.neozmq.except.ServerError;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeResource extends PropertyContainerResource {

    final public static String NAME = "node";

    final private HashMap<String, Label> labels;

    public NodeResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
        this.labels = new HashMap<>();
    }

    /**
     * GET node {node_id}
     *
     * Fetch a single node by ID.
     */
    @Override
    public PropertyContainer get(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            sendOK(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Node " + nodeID + " not found"));
        }
    }

    /**
     * PUT node {node_id} {labels} {properties}
     *
     * Replace all labels and properties on a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist.
     */
    @Override
    public PropertyContainer put(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            Lock readLock = tx.acquireReadLock(node);
            removeLabels(node);
            removeProperties(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            sendOK(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Node " + nodeID + " not found"));
        }
    }

    /**
     * PATCH node {node_id} {labels} {properties}
     *
     * Add new labels and properties to a node identified by ID.
     * This will not create a node with the given ID if one does not
     * already exist and any existing labels and properties will be
     * maintained.
     */
    @Override
    public PropertyContainer patch(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        List labelNames = request.getListData(1);
        Map properties = request.getMapData(2);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            Lock readLock = tx.acquireReadLock(node);
            addLabels(node, labelNames);
            addProperties(node, properties);
            readLock.release();
            writeLock.release();
            sendOK(node);
            return node;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Node " + nodeID + " not found"));
        }
    }

    /**
     * POST node {labels} {properties}
     *
     * Create a new node with the given labels and properties.
     */
    @Override
    public PropertyContainer post(Transaction tx, Request request) throws ClientError, ServerError {
        List labelNames = request.getListData(0);
        Map properties = request.getMapData(1);
        Node node = database().createNode();
        Lock writeLock = tx.acquireWriteLock(node);
        Lock readLock = tx.acquireReadLock(node);
        addLabels(node, labelNames);
        addProperties(node, properties);
        readLock.release();
        writeLock.release();
        sendCreated(node);
        return node;
    }

    /**
     * DELETE node {node_id}
     *
     * Delete a node identified by ID.
     */
    @Override
    public PropertyContainer delete(Transaction tx, Request request) throws ClientError, ServerError {
        long nodeID = request.getIntegerData(0);
        try {
            Node node = database().getNodeById(nodeID);
            Lock writeLock = tx.acquireWriteLock(node);
            node.delete();
            writeLock.release();
            sendNoContent();
            return null;
        } catch (NotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, "Node " + nodeID + " not found"));
        }
    }

    public void addLabels(Node node, List labelNames) {
        for (Object labelName : labelNames) {
            node.addLabel(getLabel(labelName.toString()));
        }
    }

    public void removeLabels(Node node) {
        for (Label label : node.getLabels()) {
            node.removeLabel(label);
        }
    }

    private Label getLabel(String name) {
        if (labels.containsKey(name)) {
            return labels.get(name);
        } else {
            Label label = DynamicLabel.label(name);
            labels.put(name, label);
            return label;
        }
    }

}
