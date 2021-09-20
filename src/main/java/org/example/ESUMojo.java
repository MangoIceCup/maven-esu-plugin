package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.example.entity.TestRoot;

import java.net.URI;
import java.util.Date;
import java.util.List;

@Mojo(name = "upload")
public class ESUMojo extends AbstractMojo {

    @Parameter(property = "serverAddress")
    private URI serverAddress;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "surefire-reports")
    private String surefireReports;

    @Parameter(defaultValue = "3000")
    private int bulkSize;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();
        log.info("Elastic Search Upload Plugin");
        if (mavenProject.isExecutionRoot()) {
            upload();
        } else {
            log.info("Current project is not execution root, skip upload test info.");
        }
    }

    private void upload() {
        final TimeStampIdGenerator timeStampIdGenerator = new TimeStampIdGenerator(new Date().getTime());
        final String lastCommitHash = GitTools.getLastCommitHash();
        final List<List<TestRoot>> partitions = Projects.getPartiedTestSuites(timeStampIdGenerator, lastCommitHash, mavenProject, surefireReports, bulkSize);
        try (ESUUploader esuUploader = new ESUUploader(serverAddress.getHost(), serverAddress.getPort(), serverAddress.getPath(), getLog())) {
            esuUploader.bulkUpload(partitions.stream());
        } catch (Exception e) {
            getLog().warn(e);
        }
    }


}

