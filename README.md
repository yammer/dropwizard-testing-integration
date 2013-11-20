dropwizard-testing-integration
==============================

Utilities for writing dropwizard integration tests, which allow for starting and stopping a service
without stopping the jvm.

Typically, a dropwizard service provides three components:
- API - defines the model
- Client - defines a simple interface to access the service
- Service - the dropwizard RESTful service

An integration test in this context tests the full service stack, 
i.e., the Service is started a call from the Client is issued to the Service
and a result from the API package is returned. Finally, the service is stopped.

The package provides three classes:
- TestService - a utility class that creates a server that can be started and stopped. Additionally it can prepare files for test.
- LifecycleService - a wrapper around the dropwizard service class which enables its lifecycle to be managed
- LifecycleServerCommand - the substitute for the ServerCommand which enables lifcycle management

To use the utilities include the following dependency in your pom

            <dependency>
                <groupId>com.yammer.dropwizard.testing</groupId>
                <artifactId>dropwizard-testing-integration</artifactId>
                <version>0.0.3</version>
            </dependency>

The `TestServiceIntegrationTest` is an example of how the tools can be used to write an integration test.