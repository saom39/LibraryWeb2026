package com.libraryms.util;

import java.util.UUID;

/** 主键生成工具：与数据库 char(36) 的 UUID 风格一致 */
public class IdUtil {
    public static String uuid() {
        return UUID.randomUUID().toString();
    }
}
