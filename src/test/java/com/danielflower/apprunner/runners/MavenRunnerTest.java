package com.danielflower.apprunner.runners;

import com.danielflower.apprunner.io.OutputToWriterBridge;
import com.danielflower.apprunner.problems.ProjectCannotStartException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scaffolding.Photocopier;
import scaffolding.TestConfig;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MavenRunnerTest {

    private static final HttpClient client = new HttpClient();
    private final StringBuilderWriter buildLog = new StringBuilderWriter();
    private final StringBuilderWriter consoleLog = new StringBuilderWriter();

    @BeforeClass public static void setup() throws Exception {
        client.start();
    }

    @AfterClass public static void stop() throws Exception {
        client.stop();
    }

    @Test
    public void canStartAMavenProcessByPackagingAndRunning() throws Exception {
        String appName = "maven";
        MavenRunner runner = new MavenRunner(
            Photocopier.copySampleAppToTempDir(appName),
            HomeProvider.default_java_home,
            MavenRunner.CLEAN_AND_PACKAGE);

        try {
            startAndStop(appName, runner);
            startAndStop(appName, runner);
        } catch (ProjectCannotStartException e) {
            System.out.println(buildLog);
            System.out.println(consoleLog);
            throw e;
        }
    }

    private void startAndStop(String appName, MavenRunner runner) throws Exception {
        try (Waiter startupWaiter = Waiter.waitForApp(appName, 45678)) {
            runner.start(
                new OutputToWriterBridge(buildLog),
                new OutputToWriterBridge(consoleLog),
                TestConfig.testEnvVars(45678, appName),
                startupWaiter);
        }
        try {
            ContentResponse resp = client.GET("http://localhost:45678/" + appName + "/");
            assertThat(resp.getStatus(), is(200));
            assertThat(resp.getContentAsString(), containsString("My Maven App"));
            assertThat(buildLog.toString(), containsString("[INFO] Building my-maven-app 1.0-SNAPSHOT"));
        } finally {
            runner.shutdown();
        }
    }
}
