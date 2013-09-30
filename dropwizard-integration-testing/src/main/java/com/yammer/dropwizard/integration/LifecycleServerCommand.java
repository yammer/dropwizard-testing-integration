package com.yammer.dropwizard.integration;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.EnvironmentCommand;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.EnvironmentFriend;
import com.yammer.dropwizard.config.ServerFactory;
import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static com.google.common.base.Preconditions.checkArgument;

public class LifecycleServerCommand<T extends Configuration> extends EnvironmentCommand<T> {
    public static final String COMMAND_LINE_NAME = "test-server";
    private final static Logger logger = LoggerFactory.getLogger(ServerCommand.class);
    private final Class<T> configurationClass;
    private Server server;
    private EnvironmentFriend startUpEnvironment;


    public LifecycleServerCommand(Service<T> service, Class<T> configurationClass) {
        super(service, COMMAND_LINE_NAME, "Test version of the server command, which enables server shutdown.");
        this.configurationClass = configurationClass;
    }

    /**
     * Since we don't subclass ServerCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, T configuration) throws Exception {
        this.startUpEnvironment = new EnvironmentFriend(environment); // remember the startup environment to enable full shutdown

        this.server = new ServerFactory(configuration.getHttpConfiguration(),
                environment.getName()).buildServer(environment);

        try {
            server.start();
            for (ServerLifecycleListener listener : environment.getServerListeners()) {
                listener.serverStarted(server);
            }
        } catch (Exception e) {
            logger.error("Unable to start server, shutting down", e);
            server.stop();
        }
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public void stop() throws Exception {
        try {
            stopJetty();
        } finally {
            unRegisterHealthChecks();
            unRegisterLoggingMBean();
        }
    }

    private void unRegisterLoggingMBean() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName loggerObjectName = new ObjectName("com.yammer:type=Logging");
        if (server.isRegistered(loggerObjectName)) {
            server.unregisterMBean(loggerObjectName);
        }
    }

    private void unRegisterHealthChecks() {
        for (HealthCheck healthCheck : startUpEnvironment.getHealthChecks()) {
            HealthChecks.defaultRegistry().unregister(healthCheck);
        }
    }

    private void stopJetty() throws Exception {
        if (server != null) {
            server.stop();
            checkArgument(server.isStopped());
        }
    }


}
