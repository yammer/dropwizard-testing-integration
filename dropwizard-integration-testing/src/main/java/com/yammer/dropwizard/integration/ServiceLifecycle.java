package com.yammer.dropwizard.integration;


/**
 * API needed by the
 */
public interface ServiceLifecycle {

    boolean isRunning();

    void stop() throws Exception;

    void run(String[] arguments) throws Exception;

}
