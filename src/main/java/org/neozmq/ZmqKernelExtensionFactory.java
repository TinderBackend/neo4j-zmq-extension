package org.neozmq;

/**
 * Created by gabriellipson on 3/7/14.
 */

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.Description;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.lifecycle.Lifecycle;

import static org.neo4j.helpers.Settings.*;



public class ZmqKernelExtensionFactory extends KernelExtensionFactory<ZmqKernelExtensionFactory.Dependencies> {

    @Description("Settings for the Zmq Server Extension")
    public static abstract class ZmqSettings {
        public static Setting<HostnamePort> zmq_address = setting( "zmq_address", HOSTNAME_PORT, ":47474" );
        public static Setting<Integer> zmq_threads = setting( "zmq_threads", INTEGER, "10");
        public static Setting<Boolean> zmq_enabled = setting( "zmq_enabled", BOOLEAN, "false" );
    }

    public ZmqKernelExtensionFactory() {
        super("ZMQ_SERVER");
    }

    @Override
    public Lifecycle newKernelExtension(Dependencies dependencies) throws Throwable {
        Config config = dependencies.getConfig();
        Boolean enabled = config.get(ZmqSettings.zmq_enabled);
        if (enabled) {
            return new ZmqKernelExtension(dependencies);
        } else {
            return new FakeLifecycle();
        }
    }

    public interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();
        StringLogger getStringLogger();
        Config getConfig();
    }
}

