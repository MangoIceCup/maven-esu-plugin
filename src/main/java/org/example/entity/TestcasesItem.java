package org.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestcasesItem{

	@JsonProperty("testcase_name")
	private String testcaseName;

	@JsonProperty("classname")
	private String classname;

	@JsonProperty("success")
	private boolean success;

	@JsonProperty("failure_error")
	private FailureError failureError;

	public void setClassname(String classname){
		this.classname = classname;
	}

	public String getClassname(){
		return classname;
	}

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setTestcaseName(String testcaseName){
		this.testcaseName = testcaseName;
	}

	public String getTestcaseName(){
		return testcaseName;
	}

	public void setFailureError(FailureError failureError){
		this.failureError = failureError;
	}

	public FailureError getFailureError(){
		return failureError;
	}
}