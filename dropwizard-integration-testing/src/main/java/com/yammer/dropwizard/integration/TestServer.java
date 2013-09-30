package com.yammer.dropwizard.integration;


import com.google.common.collect.Lists;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yammer.dropwizard.integration.LifecycleServerCommand.COMMAND_LINE_NAME;

/**
 * A convenience class for use in integration tests. When provided with the service under test it
 * allows for its start and shutdown. Additionally, it ensures that the config file and additional files
 * are copied from the tests resources folder to the directory from which the service is being run.
 * @param <T>
 * @param <S>
 */
public class TestServer<T extends Configuration, S extends Service<T>> {
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
     * @param serviceUnderTest an instance of {@link com.yammer.dropwizard.Service} under test. As explained in the documentation for
     * {@link LifecycleService#LifecycleService(com.yammer.dropwizard.Service)} the {@link com.yammer.dropwizard.Service#run(String[])} method should not
     *                         have been executed.
     * @param configFile the filename of the file to be parsed into the configuration class {@param T}. It expected to be located in the resources of the
     *                   test, in the same package.
     * @param additionalFiles filenames of additional file to be available at the same path as {@param configFile} when the server is starting. Likewise,
     *                        they are expected to be found in the resources for the class under test.
     * @param <T>
     * @param <S>
     * @return
     * @throws Exception
     */
    public static <T extends Configuration, S extends Service<T>> TestServer<T, S>
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
        wasRun = true;
        setUpFiles(supportingFilesPaths);
        final String[] runArgs = {COMMAND_LINE_NAME, configFilePath};
        serviceLifecycleWrapper.run(runArgs);
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
            (new File(filename)).delete();
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
