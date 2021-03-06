package e2e;

import com.danielflower.apprunner.App;
import com.danielflower.apprunner.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scaffolding.AppRepo;
import scaffolding.RestClient;

import java.io.File;
import java.util.HashMap;

import static com.danielflower.apprunner.FileSandbox.dirPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static scaffolding.ContentResponseMatcher.equalTo;

public class NodeTest {

    final String port = "48189";
    final String appRunnerUrl = "http://localhost:" + port;
    final RestClient restClient = RestClient.create(appRunnerUrl);
    final String appId = "nodejs";
    final AppRepo appRepo = AppRepo.create(appId);

    final App app = new App(new Config(new HashMap<String,String>() {{
        put(Config.SERVER_PORT, port);
        put(Config.DATA_DIR, dirPath(new File("target/datadirs/" + System.currentTimeMillis())));
    }}));

    @Before public void start() throws Exception {
        app.start();
    }

    @After public void shutdownApp() {
        restClient.stop();
        app.shutdown();
    }

    @Test
    public void canCreateAnAppViaTheRestAPI() throws Exception {
        assertThat(restClient.createApp(appRepo.gitUrl()).getStatus(), is(201));
        assertThat(restClient.deploy(appId).getStatus(), is(200));
        assertThat(restClient.homepage(appId), is(equalTo(200, containsString("Hello from nodejs!"))));
    }

}
