package org.zerograph.resources;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.EntityNotFoundException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.Request;
import org.zerograph.Response;
import org.zerograph.except.ClientError;
import org.zerograph.except.ServerError;
import org.zeromq.ZMQ;

import java.util.ArrayList;
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
        try {
            ExecutionResult result = execute(query);
            List<String> columns = result.columns();
            sendContinue(columns.toArray(new Object[columns.size()]));
            PropertyContainer firstEntity = null;
            int rowNumber = 0;
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                sendContinue(values.toArray(new Object[values.size()]));
                if (rowNumber == 0) {
                    Object firstValue = values.get(0);
                    if (firstValue instanceof PropertyContainer) {
                        firstEntity = (PropertyContainer)firstValue;
                    }
                }
                rowNumber += 1;
            }
            sendOK();
            return firstEntity;
        } catch (EntityNotFoundException ex) {
            throw new ClientError(new Response(Response.NOT_FOUND, ex.getMessage()));
        } catch (CypherException ex) {
            //ex.printStackTrace(System.err);
            throw new ClientError(new Response(Response.BAD_REQUEST, ex.getMessage()));
        }
    }

}
