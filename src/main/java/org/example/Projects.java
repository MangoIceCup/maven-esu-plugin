package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.example.entity.FailureError;
import org.example.entity.TestRoot;
import org.example.entity.TestSuit;
import org.example.entity.TestcasesItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Projects {

    private static Stream<Path> allFilesUnder(Path path) {
        try {
            final Map<Boolean, List<Path>> childes = Files.list(path).collect(Collectors.partitioningBy(Files::isDirectory));
            final List<Path> dirs = childes.getOrDefault(true, new ArrayList<>());
            final List<Path> files = childes.getOrDefault(false, new ArrayList<>());
            return Stream.concat(files.stream(), dirs.stream().flatMap(Projects::allFilesUnder));
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    public static boolean isTestSuitFile(Path path) {
        final Path file = path.toAbsolutePath().normalize();
        final String fileName = file.toFile().getName();
        final String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.startsWith("test-") && lowerCaseName.endsWith(".xml");
    }


    public static TestRoot toTestRoot(MavenProject mavenProject, final Path path, final TimeStampIdGenerator generator, final String commitId, String rootProjectName) {
        try {
            final SAXReader saxReader = new SAXReader();
            final Document document = saxReader.read(path.toFile());

            final TestRoot testRoot = new TestRoot();
            final TestSuit testSuit = new TestSuit();
            testRoot.setTestsuite(testSuit);
            testSuit.setTimestamp(generator.getTimestampString());
            testSuit.setUploadID(generator.generateUploadId());
            testSuit.setRootProjectName(rootProjectName);
            testSuit.setModuleName(mavenProject.getName());
            testSuit.setGitCommitId(commitId);
            testSuit.setTestsuiteName(extractNode(document, "/testsuite/@name"));
            testSuit.setNumOfTestcases(Integer.parseInt(Optional.ofNullable(extractNode(document, "/testsuite/@tests")).orElseGet(() -> "0")));
            testSuit.setNumOfSkipped(Integer.parseInt(Optional.ofNullable(extractNode(document, "/testsuite/@skipped")).orElseGet(() -> "0")));
            final int errorFailures = Integer.parseInt(Optional.ofNullable(extractNode(document, "/testsuite/@errors")).orElseGet(() -> "0")) + Integer.parseInt(Optional.ofNullable(extractNode(document, "/testsuite/@failures")).orElseGet(() -> "0"));
            testSuit.setNumOfFailuresErrors(errorFailures);
            testSuit.setNumOfPassed(testSuit.getNumOfTestcases() - testSuit.getNumOfSkipped() - testSuit.getNumOfFailuresErrors());
            testSuit.setSuccess(testSuit.getNumOfFailuresErrors() == 0);

            final ArrayList<TestcasesItem> testcases = new ArrayList<>();
            testSuit.setTestcases(testcases);

            final List<Node> nodes = document.selectNodes("//testcase");
            final List<TestcasesItem> testcasesItems = nodes.stream().map(node -> {
                final TestcasesItem testcasesItem = new TestcasesItem();
                testcasesItem.setTestcaseName(extractNode(node, "./@name"));
                testcasesItem.setClassname(extractNode(node, "./@classname"));
                testcasesItem.setSuccess(true);

                final Node failureNode = node.selectSingleNode("failure");
                if (failureNode != null) {
                    testcasesItem.setSuccess(false);
                    final FailureError failureError = new FailureError();
                    testcasesItem.setFailureError(failureError);
                    failureError.setType(extractNode(failureNode, "@type"));
                    failureError.setErrMsg(node.getStringValue());
                }

                final Node errorNode = node.selectSingleNode("error");
                if (errorNode != null) {
                    testcasesItem.setSuccess(false);
                    final FailureError failureError = new FailureError();
                    testcasesItem.setFailureError(failureError);
                    failureError.setType(extractNode(errorNode, "@type"));
                    failureError.setErrMsg(node.getStringValue());
                }
                return testcasesItem;
            }).collect(Collectors.toList());
            testcases.addAll(testcasesItems);
            return testRoot;
        } catch (DocumentException e) {
            return null;
        }
    }

    private static String extractNode(Node doc, String xpathExpression) {
        String name;
        final Node node = doc.selectSingleNode(xpathExpression);
        if (node != null) {
            name = node.getStringValue();
        } else {
            name = null;
        }
        return name;
    }

    public static Function<MavenProject, Stream<TestRoot>> toRootTests(final TimeStampIdGenerator generator, final String commitId, String surefireOutputDirName, String rootProjectName) {
        return project -> {
            final Path surefireRoot = Paths.get(project.getBuild().getDirectory(), surefireOutputDirName);
            final File file = surefireRoot.toFile();
            if (!file.exists() || !file.isDirectory()) {
                return Stream.empty();
            } else {
                return allFilesUnder(surefireRoot).map(Path::toAbsolutePath).map(Path::normalize).filter(Projects::isTestSuitFile)
                        .map(path -> toTestRoot(project, path, generator, commitId, rootProjectName));

            }
        };
    }

    public static Stream<MavenProject> stream(MavenProject project) {
        if (project == null) {
            return Stream.empty();
        } else {
            return Stream.of(project).flatMap(Projects::allProject);
        }
    }

    public static Predicate<MavenProject> hasSurefireReportDir(final String surefirePathName) {
        return mavenProject -> {
            final File surefireRoot = Paths.get(mavenProject.getBuild().getDirectory(), surefirePathName).toFile();
            return surefireRoot.exists() && surefireRoot.isDirectory();
        };
    }


    @SuppressWarnings("unchecked")
    private static Stream<MavenProject> allProject(MavenProject mavenProject) {
        final List<MavenProject> collectedProjects = (List<MavenProject>) mavenProject.getCollectedProjects();
        return Stream.concat(Stream.of(mavenProject), collectedProjects.stream());
    }


    public static String toBulkRequestBody(List<TestRoot> bulk) {
        final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final StringBuilder stringBuilder = new StringBuilder();
        bulk.forEach(p -> {
            try {
                final String jsonText = objectMapper.writeValueAsString(p);
                stringBuilder.append("{ \"create\": { } }").append("\n");
                stringBuilder.append(jsonText).append("\n");
            } catch (JsonProcessingException ignored) {
            }
        });
        return stringBuilder.toString();
    }
}
