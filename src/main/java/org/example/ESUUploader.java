package org.example;

import org.apache.http.HttpHost;
import org.apache.maven.plugin.logging.Log;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.example.entity.TestRoot;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ESUUploader implements AutoCloseable {
    private final String hostName;
    private final Integer port;
    private final String endpoint;
    private RestClient restClient;
    private final Log log;


    public ESUUploader(String hostName, Integer port, String endpoint, Log log) {

        Objects.requireNonNull(hostName);
        Objects.requireNonNull(port);
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(log);

        this.hostName = hostName;
        this.port = port;
        this.endpoint = endpoint;
        this.log = log;


        setupRestClient();
    }


    @Override
    public void close() throws Exception {
        if (this.restClient != null) {
            this.restClient.close();
        }
    }

    public void bulkUpload(Stream<List<TestRoot>> stream) {
        sendRequest(stream.map(Projects::toBulkRequestBody).map(content -> {
            final Request request = new Request("POST", endpoint + "/_bulk");
            request.setJsonEntity(content);
            return request;
        }));
    }



    private void sendRequest(Stream<Request> stream) {
        stream.forEach(this::sendRequest);
    }

    private void sendRequest(Request request) {
        try {
            restClient.performRequest(request);
        } catch (IOException e) {
            log.warn("send test result to elastic server fail.");
        }
    }


    private void setupRestClient() {
        restClient = RestClient.builder(new HttpHost(hostName, port, "http")).build();
    }

}
