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


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestServerIntegrationTest {
    private static final String TEST_SERVICE_URL = "http://localhost:20190";
    private static final String TEST_CONFIG = TestServerIntegrationTest.class.getResource("testConfiguration.yml").getPath();
    private static final String VALUE_FILE = "value.txt";
    private ExampleClient testClient;
    private TestServer<ExampleConfiguration, ExampleService> testServer;

    @Before
    public void setup() throws Exception {
        testServer = TestServer.create(TestServerIntegrationTest.class, new ExampleService(), TEST_CONFIG, VALUE_FILE);
        testServer.start();
        testClient = new ExampleClient(new Client(), TEST_SERVICE_URL);
    }

    @After
    public void tearDown() throws Exception {
        if (testServer.isRunning()) {
            testServer.stop();
        }
    }

    @Test
    public void when_test_server_started_then_files_loaded_correctly() throws IOException {
        assertThat(testClient.getValue(), is(equalTo(getExpectedValue())));
    }

    @Test(expected = ClientHandlerException.class)
    public void after_server_stopped_it_is_no_longer_running() throws Exception {
        testServer.stop();

        testClient.getValue();
    }

    @Test
    public void after_server_stopped_a_new_one_can_be_started() throws Exception {
        testServer.stop();

        testServer = TestServer.create(TestServerIntegrationTest.class, new ExampleService(), TEST_CONFIG, VALUE_FILE);
        testServer.start();

        testClient.getValue();
    }

    private String getExpectedValue() throws IOException {
        try (InputStream resourceInputStream = TestServerIntegrationTest.class.getResourceAsStream(VALUE_FILE)) {
            Scanner valueScanner = new Scanner(resourceInputStream);
            return valueScanner.next();
        }
    }


}
