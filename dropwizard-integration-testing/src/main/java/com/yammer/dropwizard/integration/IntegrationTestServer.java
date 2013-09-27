package com.yammer.dropwizard.integration;


import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class IntegrationTestServer<T extends ServiceLifecycle> {
    private final T testServer;
    private final List<String> filesToBeDeleted = Lists.newArrayList();
    private final String configFilePath;
    private final String[] supportingFilesPaths;
    private final Class<?> testClass;
    private boolean wasRun = false;

    public IntegrationTestServer(Class<?> testClass, T testServer, final String configFile, String... additionalFiles) throws Exception {
        checkNotNull(testServer);
        checkNotNull(configFile);
        checkNotNull(testClass);
        this.testServer = testServer;
        this.configFilePath = configFile;
        this.supportingFilesPaths = Arrays.copyOf(additionalFiles, additionalFiles.length);
        this.testClass = testClass;
    }

    private void prepareFileForTest(String name) throws IOException {
        try (InputStream secretsInputStream = testClass.getResourceAsStream(name)) {
            Files.copy(secretsInputStream, new File(name).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void start() throws Exception {
        if (wasRun) {
            throw new IllegalStateException("The server can be started only once");
        }
        wasRun = true;
        setUpFiles(supportingFilesPaths);
        final String[] runArgs = {"test-server", configFilePath};
        testServer.run(runArgs);
    }

    public boolean isStarted() {
        return wasRun && testServer.isRunning();
    }

    private void setUpFiles(String... additionalFiles) throws IOException {
        for (String filename : additionalFiles) {
            filesToBeDeleted.add(filename);
            prepareFileForTest(filename);
        }
    }

    private void cleanUpFiles() {
        for (String filename : filesToBeDeleted) {
            (new File(filename)).delete();
        }
    }

    public void stop() throws Exception {
        if (!isStarted()) {
            throw new IllegalStateException("Cannot stop a server that has not been started");
        }
        testServer.stop();
        cleanUpFiles();
    }

    public T getTestServer() {
        return testServer;
    }
}
