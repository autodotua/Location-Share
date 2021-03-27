package com.autod.locshare.model;

public class Response<T> {
    private Response() {
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private T data;
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    private void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }


    public static class Builder<T>{
        private T data;
        private int code;
        private String message;


        public Builder<T> withData(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> withCode(int resultCode) {
            this.code = resultCode;
            return this;
        }

        public Builder<T> withMessage(String resultMessage) {
            this.message = resultMessage;
            return this;
        }

        public Response<T> build() {
            Response<T> response = new Response<>();
            response.setData(data);
            response.setCode(code);
            response.setMessage(message);
            return response;
        }
    }
}
