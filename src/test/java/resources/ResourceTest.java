package resources;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.zeromq.ZMQ;

public class ResourceTest {

    protected GraphDatabaseService database;

    protected ZMQ.Context context;
    protected ZMQ.Socket client;
    protected ZMQ.Socket server;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        context = ZMQ.context(1);
        server = context.socket(ZMQ.REP);
        server.bind("inproc://test");
        client = context.socket(ZMQ.REQ);
        client.connect("inproc://test");
        client.send("");
        server.recv();
    }

    @After
    public void tearDown() {
        client.close();
        server.close();
        database.shutdown();
    }

    public void sendClose() {
        server.send("");
    }

}
