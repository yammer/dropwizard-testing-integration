package com.yammer.dropwizard.testing.integration;

import com.yammer.dropwizard.config.Configuration;

/**
 * User: mrutkowski
 * Date: 9/30/13
 * Time: 9:02 AM
 */
public class TestConfiguration extends Configuration {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
