package com.libraryms.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** 把 JDBCTool.queryMaps 的结果映射为实体（含父类字段） */
public class EntityMapper {

    public static <T> T mapToEntity(Map<String, Object> row, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Field f : JDBCTool.allFields(clazz)) {
                Object v = row.get(JDBCTool.columnName(f));
                if (v == null) continue;
                f.setAccessible(true);
                Object value = cast(v, f.getType());
                if (value != null) f.set(obj, value);
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("行数据映射实体失败: " + clazz, e);
        }
    }

    private static Object cast(Object v, Class<?> type) {
        if (type.isInstance(v)) return v;
        if (type == String.class) return v.toString();
        if (type == Integer.class || type == int.class) return ((Number) v).intValue();
        if (type == Long.class || type == long.class) return ((Number) v).longValue();
        if (type == Double.class || type == double.class) return ((Number) v).doubleValue();
        if (type == Boolean.class || type == boolean.class) {
            if (v instanceof Number) return ((Number) v).intValue() != 0;
            if (v instanceof byte[]) { byte[] b = (byte[]) v; return b.length > 0 && b[b.length - 1] != 0; }
            return Boolean.parseBoolean(v.toString());
        }
        if (type == BigDecimal.class) return new BigDecimal(v.toString());
        if (type == LocalDateTime.class) {
            if (v instanceof Timestamp) return ((Timestamp) v).toLocalDateTime();
            if (v instanceof String) return LocalDateTime.parse(((String) v).replace(' ', 'T'));
        }
        if (type == LocalDate.class && v instanceof java.sql.Date) return ((java.sql.Date) v).toLocalDate();
        return null;
    }
}
