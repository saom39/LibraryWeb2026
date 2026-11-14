package com.libraryms.util;

import com.libraryms.annotation.Column;
import com.libraryms.annotation.Id;
import com.libraryms.annotation.Table;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 轻量级 ORM 工具：基于 JDBC + 反射完成实体对象与关系表之间的映射，
 * 提供 save / update / delete / getOneById 等基础操作，以及原生 SQL 的 execute / query 便捷方法。
 * 字段扫描递归覆盖整个继承体系（Base / User 继承链），并支持 java.time 与 bit(1) 的类型转换。
 */
public class JDBCTool {

    private static String url;
    private static String username;
    private static String password;

    static {
        try (InputStream in = JDBCTool.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            Class.forName(p.getProperty("jdbc.driver"));
            url = p.getProperty("jdbc.url");
            username = p.getProperty("jdbc.username");
            password = p.getProperty("jdbc.password");
        } catch (Exception e) {
            throw new RuntimeException("初始化 JDBC 配置失败", e);
        }
    }

    /** 获取数据库连接 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(AutoCloseable... cs) {
        for (AutoCloseable c : cs) {
            if (c != null) try { c.close(); } catch (Exception ignored) { }
        }
    }

    /* ===================== 反射公共辅助 ===================== */

    /** 递归取得本类及所有父类的实例字段（父类字段排在前） */
    public static List<Field> allFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            list.addAll(0, Arrays.asList(c.getDeclaredFields()));
        }
        list.removeIf(f -> f.isSynthetic() || Modifier.isStatic(f.getModifiers()));
        return list;
    }

    /** 字段对应的列名 */
    public static String columnName(Field f) {
        Column c = f.getAnnotation(Column.class);
        if (c != null) return c.value();
        Id id = f.getAnnotation(Id.class);
        return id != null ? id.value() : f.getName();
    }

    /* ===================== 方法1：ResultSet -> List<T> ===================== */

    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            Map<String, Field> fieldMap = new HashMap<>();
            for (Field f : allFields(clazz)) {
                fieldMap.put(columnName(f).toLowerCase(), f);
            }
            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnLabel(i).toLowerCase();
                    Field f = fieldMap.get(colName);
                    if (f == null) continue;
                    Object value = rs.getObject(i);
                    if (value == null) continue;
                    f.setAccessible(true);
                    Object converted = convert(value, f.getType());
                    if (converted != null) f.set(obj, converted);
                }
                list.add(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("ResultSet 转 List 失败", e);
        }
        return list;
    }

    /** 将数据库返回的值转换为实体字段声明的类型 */
    private static Object convert(Object value, Class<?> target) {
        if (target.isInstance(value)) return value;
        if (target == LocalDateTime.class) {
            if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime();
            if (value instanceof java.util.Date)
                return LocalDateTime.ofInstant(((java.util.Date) value).toInstant(), ZoneId.systemDefault());
            if (value instanceof String) return LocalDateTime.parse(((String) value).replace(' ', 'T'));
            return null;
        }
        if (target == LocalDate.class) {
            if (value instanceof Date) return ((Date) value).toLocalDate();
            if (value instanceof String) return LocalDate.parse((String) value);
            return null;
        }
        if (target == Integer.class || target == int.class)
            return ((Number) value).intValue();
        if (target == Long.class || target == long.class)
            return ((Number) value).longValue();
        if (target == Double.class || target == double.class)
            return ((Number) value).doubleValue();
        if (target == Boolean.class || target == boolean.class) {
            if (value instanceof Number) return ((Number) value).intValue() != 0;
            if (value instanceof byte[]) {
                byte[] b = (byte[]) value;
                return b.length > 0 && b[b.length - 1] != 0;
            }
            if (value instanceof String)
                return "1".equals(value) || "true".equalsIgnoreCase((String) value);
            return false;
        }
        if (target == BigDecimal.class) return new BigDecimal(value.toString());
        if (target == java.util.Date.class && value instanceof Date)
            return new java.util.Date(((Date) value).getTime());
        if (target == String.class) return value.toString();
        return value;
    }

    /* ===================== 元数据辅助方法 ===================== */

    private static String getTableName(Class<?> clazz) {
        Table t = clazz.getAnnotation(Table.class);
        if (t == null) throw new RuntimeException("实体类缺少 @Table 注解: " + clazz);
        return t.value();
    }

    private static List<Field> getColumnFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        for (Field f : allFields(clazz)) {
            if (f.getAnnotation(Column.class) != null) list.add(f);
        }
        return list;
    }

    private static Field getIdField(Class<?> clazz) {
        for (Field f : allFields(clazz)) {
            if (f.getAnnotation(Id.class) != null) return f;
        }
        throw new RuntimeException("实体类没有 @Id 注解: " + clazz);
    }

    /* ===================== 方法2：save ===================== */

    public static <T> int save(T obj, Connection connection) {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);
        List<Field> cols = getColumnFields(clazz);
        Field idField = getIdField(clazz);

        StringBuilder sb = new StringBuilder("INSERT INTO ").append(table).append("(")
                .append(columnName(idField)).append(",");
        StringBuilder vals = new StringBuilder("VALUES(?,");
        for (int i = 0; i < cols.size(); i++) {
            sb.append(columnName(cols.get(i)));
            vals.append("?");
            if (i < cols.size() - 1) { sb.append(","); vals.append(","); }
        }
        sb.append(") ").append(vals).append(")");

        try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
            idField.setAccessible(true);
            ps.setObject(1, idField.get(obj));
            for (int i = 0; i < cols.size(); i++) {
                Field f = cols.get(i);
                f.setAccessible(true);
                ps.setObject(i + 2, f.get(obj));
            }
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("save 失败", e);
        }
    }

    /* ===================== 方法3：update ===================== */

    public static <T> int update(T obj, Connection connection) {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);
        List<Field> cols = getColumnFields(clazz);
        Field idField = getIdField(clazz);

        StringBuilder sb = new StringBuilder("UPDATE ").append(table).append(" SET ");
        for (int i = 0; i < cols.size(); i++) {
            sb.append(columnName(cols.get(i))).append("=?");
            if (i < cols.size() - 1) sb.append(",");
        }
        sb.append(" WHERE ").append(columnName(idField)).append("=?");

        try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
            for (int i = 0; i < cols.size(); i++) {
                Field f = cols.get(i);
                f.setAccessible(true);
                ps.setObject(i + 1, f.get(obj));
            }
            idField.setAccessible(true);
            ps.setObject(cols.size() + 1, idField.get(obj));
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("update 失败", e);
        }
    }

    /* ===================== 方法4：delete ===================== */

    public static <T> int delete(T obj, Connection connection) {
        Class<?> clazz = obj.getClass();
        Field idField = getIdField(clazz);
        String sql = "DELETE FROM " + getTableName(clazz) + " WHERE " + columnName(idField) + "=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            idField.setAccessible(true);
            ps.setObject(1, idField.get(obj));
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("delete 失败", e);
        }
    }

    /* ===================== 方法5：getOneById ===================== */

    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) {
        Field idField = getIdField(clazz);
        String sql = "SELECT * FROM " + getTableName(clazz) + " WHERE " + columnName(idField) + "=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = resultSetToList(rs, clazz);
                return list.isEmpty() ? null : list.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException("getOneById 失败", e);
        }
    }

    /* ============ 扩展：原生 SQL（复杂查询、事务）依旧统一走 JDBCTool ============ */

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    public static int execute(String sql, Connection connection, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("execute 失败: " + sql, e);
        }
    }

    public static int execute(String sql, Object... params) {
        try (Connection c = getConnection()) {
            return execute(sql, c, params);
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    public static <T> List<T> queryList(String sql, Connection connection, Class<T> clazz, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs, clazz);
            }
        } catch (SQLException e) {
            throw new RuntimeException("queryList 失败: " + sql, e);
        }
    }

    public static <T> List<T> queryList(String sql, Class<T> clazz, Object... params) {
        try (Connection c = getConnection()) {
            return queryList(sql, c, clazz, params);
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    private static Object normalize(Object v) {
        if (v instanceof Timestamp) return ((Timestamp) v).toLocalDateTime();
        if (v instanceof Date) return ((Date) v).toLocalDate();
        if (v instanceof byte[]) {
            byte[] b = (byte[]) v;
            return b.length > 0 && b[b.length - 1] != 0;
        }
        return v;
    }

    public static List<Map<String, Object>> queryMaps(String sql, Connection connection, Object... params) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int n = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= n; i++) row.put(meta.getColumnLabel(i), normalize(rs.getObject(i)));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("queryMaps 失败: " + sql, e);
        }
        return list;
    }

    public static List<Map<String, Object>> queryMaps(String sql, Object... params) {
        try (Connection c = getConnection()) {
            return queryMaps(sql, c, params);
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    public static Map<String, Object> queryOneMap(String sql, Object... params) {
        List<Map<String, Object>> list = queryMaps(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Object queryScalar(String sql, Object... params) {
        Map<String, Object> row = queryOneMap(sql, params);
        if (row == null) return null;
        return row.values().iterator().next();
    }
}
