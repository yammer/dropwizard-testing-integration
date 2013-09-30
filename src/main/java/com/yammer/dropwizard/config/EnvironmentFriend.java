package com.yammer.dropwizard.config;

import com.yammer.metrics.core.HealthCheck;

import java.util.Set;

// ugly hack, to tear down dropwizard
public class EnvironmentFriend {
    private final Environment environment;


    public EnvironmentFriend(Environment environment) {
        this.environment = environment;
    }

    public Set<HealthCheck> getHealthChecks() {
        return environment.getHealthChecks();
    }
}
