package com.bookings.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class ApiError implements Serializable {

    private ErrorCode errorCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private final LocalDateTime timestamp;
    private String message;

    private Map<String, String> errors;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    public ApiError(Map<String, String> errors) {
        this();
        this.errors = errors;
        this.message = "Unexpected error";
        this.errorCode = ErrorCode.UNEXPECTED_ERROR;
    }

    public ApiError(ErrorCode errorCode, String message) {
        this();
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "errorCode=" + errorCode +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}

