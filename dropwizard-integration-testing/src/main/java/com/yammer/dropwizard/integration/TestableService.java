package com.yammer.dropwizard.integration;


import com.yammer.dropwizard.cli.EnvironmentCommand;
import com.yammer.dropwizard.config.Configuration;

 // TODO iterate on the class name, update the class for appropriate lifecycle
public interface TestableService<T extends Configuration> {

    void addCommand(EnvironmentCommand<T> serverCommand);

    // TODO add dependency injection configuration

}
