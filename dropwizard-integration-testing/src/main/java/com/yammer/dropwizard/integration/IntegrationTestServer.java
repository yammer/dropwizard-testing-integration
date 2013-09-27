package com.yammer.dropwizard.integration;


import com.google.common.collect.Lists;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import static com.yammer.dropwizard.integration.TestServerCommand.COMMAND_LINE_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class IntegrationTestServer<T extends Configuration, S extends Service<T>> {
    private final ServiceLifecycleWrapper<T, S> serviceLifecycleWrapper;
    private final List<String> filesToBeDeleted = Lists.newArrayList();
    private final String configFilePath;
    private final String[] supportingFilesPaths;
    private final Class<?> testClass;
    private boolean wasRun = false;

    public static <T extends Configuration, S extends Service<T>> IntegrationTestServer<T, S> create(Class<?> testClass, S testServer,
                                                                                            final String configFile,
                                                                             String... additionalFiles) throws Exception {
        return new IntegrationTestServer<>(testClass, new ServiceLifecycleWrapper<>(testServer), configFile, additionalFiles);
    }

    /* package */ IntegrationTestServer(Class<?> testClass, ServiceLifecycleWrapper<T, S> testServer, final String configFile,
                                        String... additionalFiles) throws Exception {
        checkNotNull(testServer);
        checkNotNull(configFile);
        checkNotNull(testClass);
        this.serviceLifecycleWrapper = testServer;
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
        final String[] runArgs = {COMMAND_LINE_NAME, configFilePath};
        serviceLifecycleWrapper.run(runArgs);
    }

    public boolean isStarted() {
        return wasRun && serviceLifecycleWrapper.isRunning();
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
        serviceLifecycleWrapper.stop();
        cleanUpFiles();
    }

    public S getServiceUnderTest() {
        return serviceLifecycleWrapper.getServiceUnderTest();
    }
}
