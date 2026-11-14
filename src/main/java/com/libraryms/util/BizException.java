package com.libraryms.util;

/** 业务异常：消息直接返回给页面显示 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
