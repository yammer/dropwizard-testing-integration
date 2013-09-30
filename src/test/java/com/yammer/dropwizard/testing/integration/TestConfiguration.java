package com.yammer.dropwizard.testing.integration;

import com.yammer.dropwizard.config.Configuration;


public class TestConfiguration extends Configuration {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
