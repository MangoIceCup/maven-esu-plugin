package org.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRoot {

	@JsonProperty("testsuite")
	private TestSuit testSuit;

	public void setTestsuite(TestSuit testSuit){
		this.testSuit = testSuit;
	}

	public TestSuit getTestsuite(){
		return testSuit;
	}
}