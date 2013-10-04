package com.yammer.dropwizard.testing.integration;


import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public abstract class ServiceWrapper<C extends Configuration> extends Service<C> {

    @Override
    public void initialize(Bootstrap<C> bootstrap) {
        getDelegate().initialize(bootstrap);
    }

    @Override
    public void run(C configuration, Environment environment) throws Exception {
        getDelegate().run(configuration, environment);
    }

    protected abstract Service<C> getDelegate();
}
