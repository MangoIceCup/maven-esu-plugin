package org.example.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestSuit {

    @JsonProperty("@timestamp")
    private String timestamp;

    @JsonProperty("uploadID")
    private String uploadID;

    @JsonProperty("root_project_name")
    private String rootProjectName;

    @JsonProperty("module_name")
    private String moduleName;

    @JsonProperty("gitCommitId")
    private String gitCommitId;

    @JsonProperty("testsuite_name")
    private String testsuiteName;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("numOf_Testcases")
    private int numOfTestcases;

    @JsonProperty("numOf_Passed")
    private int numOfPassed;

    @JsonProperty("numOf_FailuresErrors")
    private int numOfFailuresErrors;

    @JsonProperty("numOf_Skipped")
    private int numOfSkipped;

    @JsonProperty("testcases")
    private List<TestcasesItem> testcases;



    public void setNumOfFailuresErrors(int numOfFailuresErrors) {
        this.numOfFailuresErrors = numOfFailuresErrors;
    }

    public int getNumOfFailuresErrors() {
        return numOfFailuresErrors;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setUploadID(String uploadID) {
        this.uploadID = uploadID;
    }

    public String getUploadID() {
        return uploadID;
    }

    public void setNumOfTestcases(int numOfTestcases) {
        this.numOfTestcases = numOfTestcases;
    }

    public int getNumOfTestcases() {
        return numOfTestcases;
    }

    public void setNumOfPassed(int numOfPassed) {
        this.numOfPassed = numOfPassed;
    }

    public int getNumOfPassed() {
        return numOfPassed;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setTestcases(List<TestcasesItem> testcases) {
        this.testcases = testcases;
    }

    public List<TestcasesItem> getTestcases() {
        return testcases;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setRootProjectName(String rootProjectName) {
        this.rootProjectName = rootProjectName;
    }

    public String getRootProjectName() {
        return rootProjectName;
    }

    public void setGitCommitId(String gitCommitId) {
        this.gitCommitId = gitCommitId;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public void setTestsuiteName(String testsuiteName) {
        this.testsuiteName = testsuiteName;
    }

    public String getTestsuiteName() {
        return testsuiteName;
    }

    public void setNumOfSkipped(int numOfSkipped) {
        this.numOfSkipped = numOfSkipped;
    }

    public int getNumOfSkipped() {
        return numOfSkipped;
    }
}