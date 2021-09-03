package org.example;

import org.apache.http.HttpHost;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ESUUploader implements AutoCloseable {
    private Path targetDir;
    private String hostName;
    private Integer port;
    private String endpoint;
    private RestClient restClient;
    private Path surefireReportsRoot;
    private Log log;


    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public ESUUploader(Path targetDir, String hostName, Integer port, String endpoint, Log log) {

        Objects.requireNonNull(targetDir);
        Objects.requireNonNull(hostName);
        Objects.requireNonNull(port);
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(log);

        this.targetDir = targetDir;
        this.hostName = hostName;
        this.port = port;
        this.endpoint = endpoint;
        this.log = log;

        surefireReportsRoot = Paths.get(this.targetDir.toAbsolutePath().normalize().toString(), "surefire-reports");

        setupRestClient();
    }

    private static Stream<? extends Path> traverseFiles(Path p) {
        if (Files.isDirectory(p)) {
            try {
                return Files.list(p).flatMap(ESUUploader::traverseFiles);
            } catch (IOException e) {
                return Stream.empty();
            }
        } else {
            return Stream.of(p);
        }
    }

    private void setupRestClient()
    {
        restClient = RestClient.builder(new HttpHost(hostName, port, "http")).build();
    }


    public void uploadTestCaseResult() {
        try {
            Files
                    .list(surefireReportsRoot)
                    .flatMap(ESUUploader::traverseFiles)
                    .filter(Objects::nonNull)
                    .filter(p -> p.toString().toLowerCase().endsWith(".xml"))
                    .map(ESUUploader::createJsonObject)
                    .filter(Objects::nonNull)
                    .map(JSONObject::toString)
                    .map(this::makePostRequest)
                    .forEach(this::sendRequest);
        } catch (IOException e) {
            log.warn("list test result of surefire fail.", e);
        }
    }

    private void settingMapping(Log log, RestClient restClient) throws IOException {
        final Request head = new Request("HEAD", endpoint);
        final Response response = restClient.performRequest(head);
        if (response.getStatusLine().getStatusCode() == 404) {
            try {
                final Request put = new Request("PUT", endpoint);
                restClient.performRequest(put);
            } catch (Exception e) {
                log.error(e);
            }
        }
        try {
            final Request put = new Request("PUT", endpoint + "/_mapping");
            put.setJsonEntity("{\"properties\":{\"testsuite.properties.property\":{\"type\":\"nested\",\"properties\":{\"value\":{\"type\":\"text\"}}}}}");
            restClient.performRequest(put);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void sendRequest(Request request) {
        try {
            restClient.performRequest(request);
        } catch (IOException e) {
            log.warn("send test result to elastic server fail.");
        }
    }

    public Path getTargetDir() {
        return targetDir;
    }

    private Request makePostRequest(String jsonText) {
        final Request post = new Request("POST", endpoint + "/_doc");
        post.setJsonEntity(jsonText);
        return post;
    }

    private static JSONObject createJsonObject(Path path) {
        if (path == null || Files.isDirectory(path)) {
            return null;
        }
        try {
            final JSONObject rootJsonObject = new JSONObject();
            rootJsonObject.put("@timestamp", simpleDateFormat.format(new Date()));
            final JSONObject suite = new JSONObject();
            rootJsonObject.put("testsuite", suite);

            final SAXReader saxReader = new SAXReader();
            final Document doc = saxReader.read(path.toFile());
            suite.put("testsuite_name", extractNode(doc, "/testsuite/@name"));
            suite.put("time", extractNode(doc, "/testsuite/@time"));
            suite.put("numberOfTestcases", extractNode(doc, "/testsuite/@tests"));
            suite.put("errors", extractNode(doc, "/testsuite/@errors"));
            suite.put("skipped", extractNode(doc, "/testsuite/@skipped"));
            suite.put("failures", extractNode(doc, "/testsuite/@failures"));

            final ArrayList<JSONObject> testcaseArray = new ArrayList<>();

            final List<Node> testcases = doc.selectNodes("/testsuite/testcase");
            for (Node testcaseNode : testcases) {
                final JSONObject testcaseObject = new JSONObject();
                testcaseArray.add(testcaseObject);
                testcaseObject.put("testcase_name", extractNode(testcaseNode, "./@name"));
                testcaseObject.put("classname", extractNode(testcaseNode, "./@classname"));
                testcaseObject.put("time", extractNode(testcaseNode, "./@time"));

                final Node failureNode = testcaseNode.selectSingleNode("failure");
                if (failureNode != null) {
                    final JSONObject failureNodeJSONObject = new JSONObject();
                    testcaseObject.put("failure", failureNodeJSONObject);
                    failureNodeJSONObject.put("type", extractNode(failureNode, "./@type"));
                    failureNodeJSONObject.put("err_msg", failureNode.getText());
                }
            }
            suite.put("testcase", testcaseArray);

            return rootJsonObject;
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


    @Override
    public void close() throws Exception {
        if (this.restClient != null) {
            this.restClient.close();
        }
    }
}
