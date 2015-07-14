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


import com.google.common.collect.Lists;
import io.dropwizard.Application;
import io.dropwizard.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yammer.dropwizard.testing.integration.LifecycleServerCommand.COMMAND_LINE_NAME;

/**
 * A convenience class for use in integration tests. When provided with the service under test it
 * allows for its start and shutdown. Additionally, it ensures that the config file and additional files
 * are copied from the tests resources folder to the directory from which the service is being run.
 * @param <T>
 * @param <S>
 */
public class TestServer<T extends Configuration, S extends Application<T>> {
    private final LifecycleService<T, S> serviceLifecycleWrapper;
    private final List<String> filesToBeDeleted = Lists.newArrayList();
    private final String configFilePath;
    private final String[] supportingFilesPaths;
    private final Class<?> testClass;
    private boolean wasRun = false;

    /* package */ TestServer(Class<?> testClass, LifecycleService<T, S> testServer, final String configFile,
                             String... additionalFiles) throws Exception {
        checkNotNull(testServer);
        checkNotNull(configFile);
        checkNotNull(testClass);
        this.serviceLifecycleWrapper = testServer;
        this.configFilePath = configFile;
        this.supportingFilesPaths = Arrays.copyOf(additionalFiles, additionalFiles.length);
        this.testClass = testClass;
    }

    /**
     *
     * @param testClass the class of the test
     * @param serviceUnderTest an instance of {@link io.dropwizard.Application} under test. As explained in the documentation for
     * {@link LifecycleService#LifecycleService(io.dropwizard.Application)} the {@link io.dropwizard.Application#run(String[])} method should not
     *                         have been executed.
     * @param configFile the path to the config file
     * @param additionalFiles filenames of additional files to be available at the same path as the server run directory. They are expected to be found in
     *                        the resources for the class under test.
     * @param <T>
     * @param <S>
     * @return
     * @throws Exception
     */
    public static <T extends Configuration, S extends Application<T>> TestServer<T, S>
    create(Class<?> testClass, S serviceUnderTest, final String configFile, String... additionalFiles) throws Exception {
        return new TestServer<>(testClass, new LifecycleService<>(serviceUnderTest), configFile, additionalFiles);
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
        setUpFiles(supportingFilesPaths);
        final String[] runArgs = {COMMAND_LINE_NAME, configFilePath};
        serviceLifecycleWrapper.run(runArgs);
        wasRun = true;
    }

    public boolean isRunning() {
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
            if (!(new File(filename)).delete()) {
                throw new IllegalStateException("Failed to delete file");
            }
        }
    }

    public void stop() throws Exception {
        if (!isRunning()) {
            throw new IllegalStateException("Cannot stop a server that has not been started");
        }
        serviceLifecycleWrapper.stop();
        cleanUpFiles();
    }

    public S getServiceUnderTest() {
        return serviceLifecycleWrapper.getServiceUnderTest();
    }
}
