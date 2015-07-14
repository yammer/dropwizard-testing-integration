/**
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS
 * OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under
 * the License.
 */
package com.yammer.dropwizard.testing.integration;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
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
    private Environment startUpEnvironment;


    public LifecycleServerCommand(Application<T> service, Class<T> configurationClass) {
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
        this.startUpEnvironment = environment; // remember the startup environment to enable full shutdown

        this.server = configuration.getServerFactory().build(environment);

        try {
            environment.lifecycle().attach(server);
            server.start();
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
        for (String healthCheck : startUpEnvironment.healthChecks().getNames()) {
            startUpEnvironment.healthChecks().unregister(healthCheck);
        }
    }

    private void stopJetty() throws Exception {
        if (server != null) {
            server.stop();
            checkArgument(server.isStopped());
        }
    }


}
