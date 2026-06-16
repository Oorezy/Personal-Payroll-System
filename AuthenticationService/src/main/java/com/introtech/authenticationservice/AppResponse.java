package com.introtech.authenticationservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonPropertyOrder({"status", "message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponse {

    private boolean status;

    private String message;

    private Object data;

    public AppResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public AppResponse(String message) {
        this.message = message;
    }
}
