package org.neozmq.resources;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.EntityNotFoundException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.cypher.javacompat.QueryStatistics;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neozmq.Request;
import org.neozmq.Response;
import org.neozmq.except.ClientError;
import org.neozmq.except.ServerError;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CypherResource extends Resource {

    final public static String NAME = "cypher";

    public CypherResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    /**
     * POST cypher {query} [{params}]
     *
     * @param request
     */
    @Override
    public PropertyContainer post(Transaction tx, Request request) throws ClientError, ServerError {
        String query = request.getStringData(0);
        Map params = request.getMapData(1);
        Map stats = new HashMap();
        long startTime=System.currentTimeMillis();
        try {
            ExecutionResult result = execute(query,params);
            List<String> columns = result.columns();
            sendContinue(columns);
            PropertyContainer firstEntity = null;
            int rowNumber = 0;
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                sendContinue(values);
                if (rowNumber == 0) {
                    Object firstValue = values.get(0);
                    if (firstValue instanceof PropertyContainer) {
                        firstEntity = (PropertyContainer)firstValue;
                    }
                }
                rowNumber += 1;
            }
            if (result != null) {
                final QueryStatistics queryStats = result.getQueryStatistics();
                if (queryStats != null && queryStats.containsUpdates()) {
                    stats.put("updates", true);
                    putIfValue(stats, "constraints_added", queryStats.getConstraintsAdded());
                    putIfValue(stats, "constraints_removed", queryStats.getConstraintsRemoved());
                    putIfValue(stats, "indexes_added", queryStats.getIndexesAdded());
                    putIfValue(stats, "indexes_removed", queryStats.getIndexesRemoved());
                    putIfValue(stats, "labels_added", queryStats.getLabelsAdded());
                    putIfValue(stats, "labels_removed", queryStats.getLabelsRemoved());
                    putIfValue(stats, "nodes_deleted", queryStats.getDeletedNodes());
                    putIfValue(stats, "nodes_created", queryStats.getNodesCreated());
                    putIfValue(stats, "rels_created", queryStats.getRelationshipsCreated());
                    putIfValue(stats, "rels_deleted", queryStats.getDeletedRelationships());
                    putIfValue(stats, "props_set", queryStats.getPropertiesSet());
                }
            }
            long endTime=System.currentTimeMillis();
            stats.put("execution_time",endTime-startTime);
            sendOK(stats);
            return firstEntity;
        } catch (EntityNotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, ex.getMessage()));
        } catch (CypherException ex) {
            //ex.printStackTrace(System.err);
            throw new ClientError(new Response(Response.BAD_REQUEST, ex.getMessage()));
        }
    }
    private void putIfValue(Map<String, Object> result, String name, int value) {
        if (value >0) {
            result.put(name, value);
        }
    }
}