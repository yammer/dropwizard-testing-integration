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
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.eclipse.jetty.http.MimeTypes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.util.Scanner;

public class ExampleService extends Service<ExampleConfiguration> {

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.setName("example-service");
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
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
