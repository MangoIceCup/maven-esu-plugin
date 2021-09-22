package org.example;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.example.entity.TestRoot;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "esu")
public class UploadComponent extends AbstractMavenLifecycleParticipant {
    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        final MavenProject topLevelProject = session.getTopLevelProject();
        final Log log = LogFactory.getLog(UploadComponent.class);
        LogUtils.setLogger(log);
        URI serverAddress = null;
        int bulkSize = 3000;
        String surefireReports = "surefire-reports";
        boolean debugEnable = false;


        final Plugin self = findSelf(topLevelProject);
        if (self != null) {
            final String pluginTriplet = self.getGroupId() + ":" + self.getArtifactId() + ":" + self.getVersion();
            final String goal = pluginTriplet + ":upload";
            HashSet<String> sameNames = new HashSet<>();
            sameNames.add(goal);
            sameNames.add("esu:upload");
            final List<String> goals = session.getGoals();
            boolean shouldUpload = goals.stream().filter(sameNames::contains).findAny().orElseGet(() -> null) != null;
            if (shouldUpload) {
                log.info("ElasticSearch Upload Plugin uploading ...");
                final Object configuration = self.getConfiguration();
                if (configuration != null) {
                    final String xml = Objects.toString(configuration);
                    final Document document;
                    try {
                        document = DocumentHelper.parseText(xml);
                        try {
                            serverAddress = URI.create(document.selectSingleNode("//serverAddress").getStringValue());
                        } catch (Exception ignored) {
                        }

                        try {
                            bulkSize = Integer.parseInt(document.selectSingleNode("//bulkSize").getStringValue());
                        } catch (NumberFormatException ignored) {
                        }
                        try {
                            surefireReports = document.selectSingleNode("//surefireReports").getStringValue();
                        } catch (Exception ignored) {
                        }
                        try {
                            debugEnable = Boolean.getBoolean(document.selectSingleNode("//debugEnable").getStringValue());
                            LogUtils.setEnable(debugEnable);
                        } catch (Exception ignored) {
                        }
                    } catch (DocumentException ignored) {
                    }
                }
                if (serverAddress == null) {
                    if (LogUtils.isEnable()) {
                        log.error("esu need specify elastic server address");
                    }
                } else {
                    final TimeStampIdGenerator timeStampIdGenerator = new TimeStampIdGenerator(new Date().getTime());
                    final String lastCommitHash = GitTools.getLastCommitHash();
                    final List<List<TestRoot>> partitions = Projects.getPartiedTestSuites(timeStampIdGenerator, lastCommitHash, topLevelProject, surefireReports, bulkSize);

                    try (ESUUploader esuUploader = new ESUUploader(serverAddress.getHost(), serverAddress.getPort(), serverAddress.getPath(), log)) {
                        esuUploader.bulkUpload(partitions.stream());
                    } catch (Exception e) {
                        if (LogUtils.isEnable()) {
                            log.warn(e);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Plugin findSelf(MavenProject mavenProject) {
        final List<Plugin> buildPlugins = mavenProject.getBuildPlugins();
        for (Plugin plugin : buildPlugins) {
            if (Objects.equals("org.example", plugin.getGroupId()) && Objects.equals("esu-maven-plugin", plugin.getArtifactId())) {
                return plugin;
            }
        }
        return null;
    }
}
