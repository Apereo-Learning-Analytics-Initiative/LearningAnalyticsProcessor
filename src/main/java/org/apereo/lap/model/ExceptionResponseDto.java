package org.apereo.lap.model;

public class ExceptionResponseDto {
    
    private Long timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;


    public Long getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public Integer getStatus() {
        return status;
    }


    public void setStatus(Integer status) {
        this.status = status;
    }


    public String getError() {
        return error;
    }


    public void setError(String error) {
        this.error = error;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public String toString() {
        return "ExceptionResponseDto [timestamp=" + timestamp + ", status=" + status + ", error=" + error + ", message="
                + message + ", path=" + path + "]";
    }
}
