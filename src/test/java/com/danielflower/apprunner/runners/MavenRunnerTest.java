package com.danielflower.apprunner.runners;

import com.danielflower.apprunner.FileSandbox;
import com.danielflower.apprunner.io.OutputToWriterBridge;
import com.danielflower.apprunner.mgmt.AppManager;
import com.danielflower.apprunner.problems.AppRunnerException;
import com.danielflower.apprunner.problems.ProjectCannotStartException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MavenRunnerTest {

    private static HttpClient client;
    StringBuilderWriter buildLog = new StringBuilderWriter();
    StringBuilderWriter consoleLog = new StringBuilderWriter();

    @BeforeClass
    public static void setup() throws Exception {
        client = new HttpClient();
        client.start();
    }

    @AfterClass
    public static void stop() throws Exception {
        client.stop();
    }

    @Test
    public void canStartAMavenProcessByPackagingAndRunning() throws InterruptedException, ExecutionException, TimeoutException {

        String appName = "maven";
        MavenRunner runner = new MavenRunner(sampleAppDir(appName), JavaHomeProvider.default_java_home, MavenRunner.CLEAN_AND_PACKAGE);
        try {
            runner.start(new OutputToWriterBridge(buildLog), new OutputToWriterBridge(consoleLog), AppManager.createAppEnvVars(45678, appName, URI.create("http://localhost")));

            try {
                ContentResponse resp = client.GET("http://localhost:45678/" + appName + "/");
                assertThat(resp.getStatus(), is(200));
                assertThat(resp.getContentAsString(), containsString("My Maven App"));
                assertThat(buildLog.toString(), containsString("[INFO] Building my-maven-app 1.0-SNAPSHOT"));
            } finally {
                runner.shutdown();
            }
        } catch (ProjectCannotStartException e) {
            System.out.println(buildLog);
            System.out.println(consoleLog);
            throw e;
        }
    }

    public static File sampleAppDir(String subDir) {
        File samples = new File("src/main/resources/samples/" + subDir);
        if (!samples.isDirectory()) {
            throw new AppRunnerException("Could not find sample dir at " + FileSandbox.dirPath(samples));
        }
        return samples;
    }
}
