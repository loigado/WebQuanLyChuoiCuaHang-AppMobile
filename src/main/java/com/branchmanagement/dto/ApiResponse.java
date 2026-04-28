package com.branchmanagement.dto;

/**
 * Response chuẩn cho tất cả API Format: { "success": true/false, "message":
 * "...", "data": {...} }
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // Manual builder
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final ApiResponse<T> instance = new ApiResponse<>();

        public Builder<T> success(boolean success) {
            instance.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            instance.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            instance.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            return instance;
        }
    }

    // Factory method tiện lợi
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}