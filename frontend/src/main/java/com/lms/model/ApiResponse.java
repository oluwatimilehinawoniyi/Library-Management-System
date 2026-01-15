package com.lms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("error")
    private ErrorDetails error;

    @JsonProperty("timestamp")
    private String timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetails {
        @JsonProperty("code")
        private String code;

        @JsonProperty("details")
        private Object details;
    }

    public String getErrorMessage() {
        if (error != null && error.getCode() != null) {
            return message + " (Error: " + error.getCode() + ")";
        }
        return message != null ? message : "An unknown error occurred";
    }
}