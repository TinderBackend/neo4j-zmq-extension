package org.neozmq;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.PropertyContainer;
import org.neozmq.except.ClientError;
import org.neozmq.util.Data;
import org.neozmq.util.Pointer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request is tab-separated string of terms
 *
 * VERB resource [data [data ...]]
 *
 */
public class Request {

    final private static ObjectMapper mapper = new ObjectMapper();

    final private String string;
    final private String method;
    final private String resource;
    final private Object[] data;

    public Request(String string) throws ClientError {
        this.string = string;
        String[] parts = string.split("\t");
        if (parts.length < 2) {
            throw new ClientError(new Response(Response.BAD_REQUEST, string));
        }
        this.method = parts[0];
        this.resource = parts[1];
        int dataSize = parts.length - 2;
        ArrayList<Object> data = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            try {
                data.add(Data.decode(parts[i + 2]));
            } catch (IOException ex) {
                throw new ClientError(new Response(Response.BAD_REQUEST, parts[i + 2]));
            }
        }
        this.data = data.toArray(new Object[dataSize]);
    }

    public String toString() {
        return this.string;
    }

    public String getMethod() {
        return this.method;
    }

    public String getResource() {
        return this.resource;
    }

    public Object getData(int index) {
        if (index >= 0 && index < this.data.length) {
            return this.data[index];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public Integer getIntegerData(int index) {
        Object datum = getData(index);
        if (datum instanceof Integer) {
            return (Integer)datum;
        } else {
            throw new IllegalArgumentException("Integer data expected");
        }
    }

    public String getStringData(int index) {
        Object datum = getData(index);
        if (datum instanceof String) {
            return (String)datum;
        } else {
            throw new IllegalArgumentException("String data expected");
        }
    }

    public List getListData(int index) {
        Object datum = getData(index);
        if (datum instanceof List) {
            return (List)datum;
        } else {
            throw new IllegalArgumentException("List data expected");
        }
    }

    public Map getMapData(int index) {
        Object datum = getData(index);
        if (datum instanceof Map) {
            return (Map)datum;
        } else {
            throw new IllegalArgumentException("Map data expected");
        }
    }

    public void resolvePointers(List<PropertyContainer> values) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Pointer) {
                Pointer pointer = (Pointer)data[i];
                data[i] = values.get(pointer.getAddress());
            }
        }
    }

}
