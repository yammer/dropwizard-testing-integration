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


import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Cli;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;

/**
 * A wrapper around a service that exposes the {@link LifecycleServerCommand} and through it
 * allows for the management of the services lifecycle, i.e., shutting it down.
 *
 *
 * @param <T> configuration type
 * @param <S> service type
 */
public class LifecycleService<T extends Configuration, S extends Service<T>> {
    private final S serviceUnderTest;
    private final LifecycleServerCommand<T> testServerCommand;

    /**
     * Take a newly created instance of the a {@link com.yammer.dropwizard.Service}. It is important that
     * the {@link com.yammer.dropwizard.Service#run(String[])}  method has not been run and the {@link #run(String[])}
     * method as defined in this class is used to start the service.
     */
    public LifecycleService(S serviceUnderTest) {
        this.serviceUnderTest = serviceUnderTest;
        this.testServerCommand = new LifecycleServerCommand(serviceUnderTest, serviceUnderTest.getConfigurationClass());
    }

    public boolean isRunning() {
        return testServerCommand.isRunning();
    }

    public void stop() throws Exception {
        testServerCommand.stop();
    }

    public void run(String[] arguments) throws Exception {
        final Bootstrap<T> bootstrap = new Bootstrap<>(serviceUnderTest);
        bootstrap.addCommand(testServerCommand);
        serviceUnderTest.initialize(bootstrap);
        final Cli cli = new Cli(serviceUnderTest.getClass(), bootstrap);
        cli.run(arguments);
    }

    public S getServiceUnderTest() {
        return serviceUnderTest;
    }
}
