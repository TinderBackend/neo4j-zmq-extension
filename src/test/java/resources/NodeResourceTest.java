package resources;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neozmq.Request;
import org.neozmq.except.ClientError;
import org.neozmq.except.ServerError;
import org.neozmq.resources.NodeResource;

import java.util.Arrays;
import java.util.HashMap;

public class NodeResourceTest extends ResourceTest {

    protected NodeResource resource;

    @Before
    public void createResource() {
        resource = new NodeResource(database, server);
    }

    protected Node createAlice() {
        Node created = database.createNode();
        resource.addLabels(created, Arrays.asList("Person"));
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Alice");
        resource.addProperties(created, properties);
        return created;
    }

    protected void assertAlice(Node node) {
        assert node.hasLabel(DynamicLabel.label("Person"));
        assert node.hasProperty("name");
        assert node.getProperty("name").equals("Alice");
    }

    @Test
    public void testCanGetExistingNode() throws ClientError, ServerError {
        String rq = "GET\tnode\t0";
        String rs = "200\t{\"id\":0,\"$neo_type\":\"NODE\",\"labels\":[\"Person\"],\"properties\":{\"name\":\"Alice\"}}";
        try (Transaction tx = database.beginTx()) {
            Node created = createAlice();
            assert created.getId() == 0;
            PropertyContainer got = resource.get(tx, new Request(rq));
            assert got instanceof Node;
            assertAlice((Node)got);
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

    @Test
    public void testCannotGetNonExistentNode() throws ClientError, ServerError {
        String rq = "GET\tnode\t0";
        String rs = "";
        try (Transaction tx = database.beginTx()) {
            try {
                resource.get(tx, new Request(rq));
                assert false;
            } catch (ClientError err) {
                assert true;
            }
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

    @Test
    public void testCanPutExistingNode() throws ClientError, ServerError {
        String rq = "PUT\tnode\t0\t[\"Person\"]\t{\"name\":\"Alice\"}";
        String rs = "200\t{\"id\":0,\"$neo_type\":\"NODE\",\"labels\":[\"Person\"],\"properties\":{\"name\":\"Alice\"}}";
        try (Transaction tx = database.beginTx()) {
            Node created = database.createNode();
            assert created.getId() == 0;
            PropertyContainer got = resource.put(tx, new Request(rq));
            assert got instanceof Node;
            assertAlice((Node)got);
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

    @Test
    public void testCannotPutNonExistentNode() throws ClientError, ServerError {
        String rq = "PUT\tnode\t0\t[\"Person\"]\t{\"name\":\"Alice\"}";
        String rs = "";
        try (Transaction tx = database.beginTx()) {
            try {
                resource.put(tx, new Request(rq));
                assert false;
            } catch (ClientError err) {
                assert true;
            }
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

    @Test
    public void testCanPatchExistingNode() throws ClientError, ServerError {
        String rq = "PATCH\tnode\t0\t[\"Female\"]\t{\"age\":33}";
        String rs = "200\t{\"id\":0,\"$neo_type\":\"NODE\",\"labels\":[\"Person\",\"Female\"],\"properties\":{\"name\":\"Alice\",\"age\":33}}";
        try (Transaction tx = database.beginTx()) {
            Node created = createAlice();
            assert created.getId() == 0;
            PropertyContainer got = resource.patch(tx, new Request(rq));
            assert got instanceof Node;
            Node gotNode = (Node)got;
            assertAlice(gotNode);
            assert gotNode.hasLabel(DynamicLabel.label("Female"));
            assert gotNode.hasProperty("age");
            assert gotNode.getProperty("age").equals(33);
        }
        sendClose();
        assert client.recvStr().startsWith("200");
    }

    @Test
    public void testCannotPatchNonExistentNode() throws ClientError, ServerError {
        String rq = "PATCH\tnode\t0\t[\"Female\"]\t{\"age\":33}";
        String rs = "";
        try (Transaction tx = database.beginTx()) {
            try {
                resource.put(tx, new Request(rq));
                assert false;
            } catch (ClientError err) {
                assert true;
            }
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

    @Test
    public void testCanCreateNode() throws ClientError, ServerError {
        String rq = "POST\tnode\t[\"Person\"]\t{\"name\":\"Alice\"}";
        String rs = "201\t{\"id\":0,\"$neo_type\":\"NODE\",\"labels\":[\"Person\"],\"properties\":{\"name\":\"Alice\"}}";
        try (Transaction tx = database.beginTx()) {
            PropertyContainer created = resource.post(tx, new Request(rq));
            assert created instanceof Node;
            Node node = (Node)created;
            assert node.hasLabel(DynamicLabel.label("Person"));
            assert node.hasProperty("name");
            assert node.getProperty("name").equals("Alice");
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

    @Test
    public void testCanDeleteNode() throws ClientError, ServerError {
        String rq = "DELETE\tnode\t0";
        String rs = "204";
        try (Transaction tx = database.beginTx()) {
            Node created = database.createNode();
            assert created.getId() == 0;
            resource.delete(tx, new Request(rq));
        }
        try (Transaction tx = database.beginTx()) {
            try {
                database.getNodeById(0);
                assert false;
            } catch (NotFoundException ex) {
                assert true;
            }
        }
        sendClose();
        assert client.recvStr().equals(rs);
    }

}
