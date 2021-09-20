package org.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FailureError {
    @JsonProperty("type")
    private String type;

    @JsonProperty("err_msg")
    private String errMsg;


    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}