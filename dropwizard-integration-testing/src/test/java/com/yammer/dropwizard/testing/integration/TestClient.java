package com.yammer.dropwizard.testing.integration;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class TestClient {
    private final WebResource rootResource;

    public TestClient(Client client, String testServiceUrl) {
        this.rootResource = client.resource(testServiceUrl);
    }

    public String getValue() {
        return rootResource.get(String.class);
    }

}
