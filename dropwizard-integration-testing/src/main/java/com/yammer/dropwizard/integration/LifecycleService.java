package com.yammer.dropwizard.integration;


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
        final Cli cli = new Cli(this.getClass(), bootstrap);
        cli.run(arguments);
    }

    public S getServiceUnderTest() {
        return serviceUnderTest;
    }
}
