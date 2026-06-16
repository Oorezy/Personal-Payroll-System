package com.introtech.introtechservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"status", "message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponse<T> {

    private boolean status;
    private String message;
    private T data;


    public AppResponse(String message) {
        this.message = message;
    }

    public AppResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

}
