package org.example;

import org.apache.http.HttpHost;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLParserConfiguration;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "upload", defaultPhase = LifecyclePhase.TEST)
public class ESUMojo extends AbstractMojo {

    @Parameter(property = "serverAddress")
    private URI serverAddress;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "surefire-reports")
    private String surefireReports;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();
        if (serverAddress == null || serverAddress.toString().trim().isEmpty()) {
            log.warn("No elastic search endpoint configured.");
            return;
        }

        final String targetDirectory = mavenProject.getBuild().getDirectory();
        log.info("Elastic Search Upload Plugin");
        log.info("Server Address: " + serverAddress);

        try (ESUUploader esuUploader = new ESUUploader(Paths.get(targetDirectory), serverAddress.getHost(), serverAddress.getPort(), serverAddress.getPath(), log)) {
            esuUploader.uploadTestCaseResult();
        } catch (Exception e) {
            log.warn(e);
        }
    }






}
