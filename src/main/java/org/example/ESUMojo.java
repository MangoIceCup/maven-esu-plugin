package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.URI;

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

    @Parameter(defaultValue = "false")
    private boolean debugEnable = false;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (mavenProject.isExecutionRoot()) {
            final Settings settings = new Settings(serverAddress, mavenProject, surefireReports, bulkSize,getLog());
            Settings.setSettings(settings);
        }
    }



}

