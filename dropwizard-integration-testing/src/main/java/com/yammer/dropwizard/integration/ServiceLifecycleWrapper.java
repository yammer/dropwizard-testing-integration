package com.yammer.dropwizard.integration;


import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Cli;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;

public class ServiceLifecycleWrapper<T extends Configuration, S extends Service<T>> {
    private final S serviceUnderTest;
    private final TestServerCommand<T> testServerCommand;

    public ServiceLifecycleWrapper(S serviceUnderTest) {
        this.serviceUnderTest = serviceUnderTest;
        this.testServerCommand = new TestServerCommand(serviceUnderTest, serviceUnderTest.getConfigurationClass());
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
        final Cli cli = new Cli(this.getClass(), bootstrap);
        cli.run(arguments);
    }

    public S getServiceUnderTest() {
        return serviceUnderTest;
    }
}
