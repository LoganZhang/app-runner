package com.danielflower.apprunner;

import com.danielflower.apprunner.mgmt.AppDescription;
import com.danielflower.apprunner.mgmt.AppManager;
import com.danielflower.apprunner.problems.AppNotFoundException;
import com.danielflower.apprunner.web.ProxyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppEstate {
    public static final Logger log = LoggerFactory.getLogger(AppEstate.class);

    private final List<AppDescription> managers = new ArrayList<>();
    private final ProxyMap proxyMap;
    private final FileSandbox fileSandbox;

    public AppEstate(ProxyMap proxyMap, FileSandbox fileSandbox) {
        this.proxyMap = proxyMap;
        this.fileSandbox = fileSandbox;
    }

    public void add(AppDescription appMan) {
        this.managers.add(appMan);
    }

    public Stream<AppDescription> all() {
        return managers.stream();
    }

    public void shutdown() {
        for (AppDescription manager : managers) {
            log.info("Stopping " + manager.name());
            try {
                manager.stopApp();
            } catch (Exception e) {
                log.warn("Error while stopping " + manager.name(), e);
            }
        }

    }

    public AppDescription addApp(String gitUrl) throws Exception {
        AppManager appMan = AppManager.create(gitUrl, fileSandbox);
        appMan.addListener(proxyMap::add);
        this.add(appMan);
        return appMan;
    }


    public void update(String name, Writer writer) throws Exception {
        for (AppDescription manager : managers) {
            if (manager.name().equalsIgnoreCase(name)) {
                manager.update(writer);
                return;
            }
        }
        String valid = all()
            .sorted((o1, o2) -> o1.name().compareTo(o2.name()))
            .map(AppDescription::name)
            .collect(Collectors.joining(", "));
        throw new AppNotFoundException("No app found with name '" + name + "'. Valid names: " + valid);
    }
}