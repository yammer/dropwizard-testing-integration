package com.yammer.dropwizard.integration;


import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;

public class TestableServiceLifecycle<T extends Configuration, S extends Service<T> & TestableService<T>> implements ServiceLifecycle {
    private final S testableService;
    private TestServerCommand<T> testServerCommand;

    public TestableServiceLifecycle(S testableService) {
        this.testableService = testableService;
        testServerCommand = new TestServerCommand(testableService, testableService.getConfigurationClass());
        testableService.addCommand(testServerCommand);
    }

    @Override
    public boolean isRunning() {
        return testServerCommand.isRunning();
    }

    @Override
    public void stop() throws Exception {
        testServerCommand.stop();
    }

    @Override
    public void run(String[] arguments) throws Exception {
        testableService.run(arguments);
    }
}
