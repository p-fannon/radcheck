package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class ApiError {
    @SerializedName("status")
    private int status;
    @SerializedName("error")
    private String error;
    @SerializedName("message")
    private String message;

    public ApiError() {
    }

    public ApiError(int status, String errorCode, String message) {
        this.status=status;
        this.error=errorCode;
        this.message=message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status=status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error=error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message=message;
    }
}
