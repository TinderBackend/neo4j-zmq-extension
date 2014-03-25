package org.neozmq.resources;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.zeromq.ZMQ;

import java.util.Map;

public abstract class PropertyContainerResource extends Resource {

    public PropertyContainerResource(GraphDatabaseService database, ZMQ.Socket socket) {
        super(database, socket);
    }

    public void addProperties(PropertyContainer entity, Map properties) {
        for (Object key : properties.keySet()) {
            entity.setProperty(key.toString(), properties.get(key));
        }
    }

    public void removeProperties(PropertyContainer entity) {
        for (Object key : entity.getPropertyKeys()) {
            entity.removeProperty(key.toString());
        }
    }

}
