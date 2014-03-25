package org.neozmq.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static org.neo4j.helpers.collection.MapUtil.map;

public class Data {

    final private static String POINTER_HINT = "/*Pointer*/";

    final private static ObjectMapper mapper = new ObjectMapper();

    private static Object decodePointer(String string) throws IOException {
        int address = mapper.readValue(string, Integer.class);
        return new Pointer(address);
    }

    public static String encode(Object value) throws IOException {
        final Object converted = convert(value);
        return mapper.writeValueAsString(converted);
    }

    public static Object decode(String string) throws IOException {
        switch (string) {
            case "null":
                return null;
            case "true":
                return true;
            case "false":
                return false;
            default:
                if (string.length() > 0) {
                    char ch = string.charAt(0);
                    if ((ch >= '0' && ch <= '9') || ch == '-') {
                        return mapper.readValue(string, Number.class);
                    } else if (ch == '"') {
                        return mapper.readValue(string, String.class);
                    } else if (ch == '[') {
                        return mapper.readValue(string, List.class);
                    } else if (ch == '{') {
                        return mapper.readValue(string, Map.class);
                    } else if (ch == '/') {
                        if (string.startsWith(POINTER_HINT)) {
                            return decodePointer(string.substring(POINTER_HINT.length()));
                        } else {
                            throw new IllegalArgumentException(string);
                        }
                    } else {
                        throw new IllegalArgumentException(string);
                    }
                } else {
                    throw new IllegalArgumentException(string);
                }
        }
    }

    private static Object convert(Object value) {
        if (value == null) return null;
        if (value instanceof Node) {
            final Node node = (Node) value;
            return map("$neo_type","NODE","id",node.getId(),"labels",labels(node),"properties",properties(node));
        }
        if (value instanceof Relationship) {
            Relationship relationship = (Relationship) value;
            return map("$neo_type", "REL", "id", relationship.getId(), "properties", properties(relationship), "start", relationship.getStartNode().getId(), "end", relationship.getEndNode().getId(), "type", relationship.getType().name());
        }
        if (value instanceof Path) {
            Path path = (Path) value;
            return map(
                    "$neo_type","PATH",
                    "length", path.length(),
                    "start", convert(path.startNode()),
                    "end", convert(path.endNode()),
                    "nodes", convert(path.nodes()),
                    "relationships", convert(path.relationships()));
        }
        if (value instanceof Iterable) {
            return convert(((Iterable) value).iterator());
        }
        if (value instanceof Iterator) {
            final ArrayList<Object> result = new ArrayList<Object>();
            Iterator iterator = (Iterator) value;
            while (iterator.hasNext()) {
                result.add(convert(iterator.next()));
            }
            return result;
        }

        if (value.getClass().isArray()) {
            final ArrayList<Object> result = new ArrayList<Object>();
            final int length = Array.getLength(value);
            for (int i=0;i< length;i++) {
                result.add(convert(Array.get(value,i)));
            }
            return result;
        }
        return value;
}
    

    private static Map<String,Object> toMap(PropertyContainer pc) {
        final Iterator<String> propertyKeys = pc.getPropertyKeys().iterator();
        if (!propertyKeys.hasNext()) return null;
        Map<String,Object> result = new LinkedHashMap<String,Object>();
        while (propertyKeys.hasNext()) {
            String prop = propertyKeys.next();
            final Object value = pc.getProperty(prop);
            if (value.getClass().isArray()) {
                final int length = Array.getLength(value);
                final ArrayList<Object> list = new ArrayList<Object>(length);
                for (int i=0;i< length;i++) {
                    list.add(Array.get(value, i));
                }
                result.put(prop, list);
            } else {
                result.put(prop, value);
            }
        }
        return result;
    }

    private static List<String> labels(Node node) {
        ArrayList<String> labelList = new ArrayList<>();
        for (Label label : node.getLabels()) {
            labelList.add(label.name());
        }
        return labelList;
    }

    private static Map<String, Object> properties(PropertyContainer entity) {
        HashMap<String, Object> propertyMap = new HashMap<>();
        for (String key : entity.getPropertyKeys()) {
            propertyMap.put(key, entity.getProperty(key));
        }
        return propertyMap;
    }
}
