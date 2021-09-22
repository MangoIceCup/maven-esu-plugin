package org.example;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.net.URI;

public class Settings {
    private static Settings settings;

    public static void setSettings(Settings settings) {
        synchronized (Settings.class) {
            Settings.settings = settings;
        }
    }

    public static Settings getSettings() {
        synchronized (Settings.class) {
            return settings;
        }
    }

    private final URI serverAddress;
    private final MavenProject mavenProject;
    private final String surefireReports;
    private final int bulkSize;
    private final Log log;

    public Settings(URI serverAddress, MavenProject mavenProject, String surefireReports, int bulkSize, Log log) {
        this.serverAddress = serverAddress;
        this.mavenProject = mavenProject;
        this.surefireReports = surefireReports;
        this.bulkSize = bulkSize;
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    public URI getServerAddress() {
        return serverAddress;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public String getSurefireReports() {
        return surefireReports;
    }

    public int getBulkSize() {
        return bulkSize;
    }
}
