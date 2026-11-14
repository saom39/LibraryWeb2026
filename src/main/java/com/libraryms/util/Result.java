package com.libraryms.util;

/** 统一响应结构：{success, message, data} */
public class Result {
    private boolean success;
    private String message;
    private Object data;

    public Result() { }

    public Result(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Result ok(Object data) {
        return new Result(true, "操作成功", data);
    }

    public static Result ok(String message, Object data) {
        return new Result(true, message, data);
    }

    public static Result fail(String message) {
        return new Result(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
