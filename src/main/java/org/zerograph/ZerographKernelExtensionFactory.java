package org.zerograph;

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



public class ZerographKernelExtensionFactory extends KernelExtensionFactory<ZerographKernelExtensionFactory.Dependencies> {

    @Description("Settings for the Zerograph Server Extension")
    public static abstract class ZerographSettings {
        public static Setting<HostnamePort> zerograph_address = setting( "zerograph_address", HOSTNAME_PORT, ":47474" );
        public static Setting<Integer> zerograph_threads = setting( "zerograph_threads", INTEGER, "10");
        public static Setting<Boolean> zerograph_enabled = setting( "zerograph_enabled", BOOLEAN, "false" );
    }

    public ZerographKernelExtensionFactory() {
        super(ZerographServer.SERVICE_NAME);
    }

    @Override
    public Lifecycle newKernelExtension(Dependencies dependencies) throws Throwable {
        Config config = dependencies.getConfig();
        Boolean enabled = config.get(ZerographSettings.zerograph_enabled);
        if (enabled) {
            Service.WORKER_COUNT = config.get(ZerographSettings.zerograph_threads) ;
            Service.DATABASE = dependencies.getGraphDatabaseService();
            Integer PORT = config.get(ZerographSettings.zerograph_address).getPort();
            Service service = new Service(PORT);
            return service;
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


class FakeLifecycle implements Lifecycle{

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void start() throws Throwable {

    }

    @Override
    public void stop() throws Throwable {

    }

    @Override
    public void shutdown() throws Throwable {

    }
}