package com.yammer.dropwizard.testing.integration;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.eclipse.jetty.http.MimeTypes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.util.Scanner;

public class TestService extends Service<TestConfiguration> {

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.setName("test-service");
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        File valueFile = new File(configuration.getFilename());
        Scanner valueScanner = new Scanner(valueFile);
        final String value = valueScanner.next();

        environment.addResource(new TestResource(value));
    }

    @Path("/")
    @Produces(MimeTypes.TEXT_PLAIN)
    public class TestResource {
        private final String value;

        public TestResource(String value) {
            this.value = value;
        }

        @GET
        public String getValue() {
            return value;
        }

    }
}
