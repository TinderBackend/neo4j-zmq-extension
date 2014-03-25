package org.neozmq.resources;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neozmq.Request;
import org.neozmq.Response;
import org.neozmq.except.ClientError;
import org.neozmq.except.ServerError;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class NodeSetResource extends Resource {

    final public static String NAME = "nodeset";

    public NodeSetResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * GET nodeset {label} {key} {value}
     *
     * tx.find(label, key, value)
     *
     * MATCH-RETURN
     * No locking
     *
     * @param request
     */
    @Override
    public PropertyContainer get(Transaction tx, Request request) throws ClientError, ServerError {
        Label label = DynamicLabel.label(request.getStringData(0));
        String key = request.getStringData(1);
        Object value = request.getData(2);
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("nodes_matched", 0);
        Node firstNode = null;
        for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
            sendContinue(node);
            if (firstNode == null) {
                firstNode = node;
            }
            stats.put("nodes_matched", stats.get("nodes_matched") + 1);
        }
        sendOK(stats);
        return firstNode;
    }

    /**
     * PUT nodeset {label} {key} {value}
     *
     * tx.merge(label, key, value)
     *
     * MERGE-RETURN
     * No locking(?)
     *
     * @param request
     */
    @Override
    public PropertyContainer put(Transaction tx, Request request) throws ClientError, ServerError {
        String labelName = request.getStringData(0);
        String key = request.getStringData(1);
        Object value = request.getData(2);
        try {
            HashMap<String, Integer> stats = new HashMap<>();
            String query = "MERGE (a:`" + labelName.replace("`", "``") +
                    "` {`" + key.replace("`", "``") + "`:{value}}) RETURN a";
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("value", value);
            ExecutionResult result = execute(query, params);
            Node firstNode = null;
            for (Map<String, Object> row : result) {
                Node node = (Node)row.get("a");
                sendContinue(node);
                if (firstNode == null) {
                    firstNode = node;
                }
            }
            int nodesCreated = result.getQueryStatistics().getNodesCreated();
            stats.put("nodes_created", nodesCreated);
            if (nodesCreated == 0) {
                sendOK(stats);
            } else {
                sendCreated(stats);
            }
            return firstNode;
        } catch (CypherException ex) {
            throw new ServerError(new Response(Response.SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * DELETE nodeset {label} {key} {value}
     *
     * tx.purge(label. key, value)
     *
     * MATCH-DELETE
     *
     * @param request
     */
    @Override
    public PropertyContainer delete(Transaction tx, Request request) throws ClientError, ServerError {
        Label label = DynamicLabel.label(request.getStringData(0));
        String key = request.getStringData(1);
        Object value = request.getData(2);
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("nodes_deleted", 0);
        for (Node node : database().findNodesByLabelAndProperty(label, key, value)) {
            node.delete();
            stats.put("nodes_deleted", stats.get("nodes_deleted") + 1);
        }
        sendOK(stats);
        return null;
    }

}
