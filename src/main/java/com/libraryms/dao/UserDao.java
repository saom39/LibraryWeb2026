package com.libraryms.dao;

import com.libraryms.entity.User;
import com.libraryms.util.BizException;
import com.libraryms.util.JDBCTool;
import com.libraryms.util.UserFactory;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 用户数据访问层：基于 JDBCTool 完成用户的查询、登录与保存 */
public class UserDao {

    private int count(String sql, Object... params) {
        Object v = JDBCTool.queryScalar(sql, params);
        return v == null ? 0 : ((Number) v).intValue();
    }

    public boolean existsName(String name) {
        return count("SELECT COUNT(*) FROM user WHERE name=?", name) > 0;
    }

    public boolean existsPhone(String phone) {
        return count("SELECT COUNT(*) FROM user WHERE phone=?", phone) > 0;
    }

    public User findById(String id) {
        Map<String, Object> row = JDBCTool.queryOneMap("SELECT * FROM user WHERE id=?", id);
        return row == null ? null : UserFactory.fromMap(row);
    }

    public User login(String name, String password) {
        Map<String, Object> row = JDBCTool.queryOneMap(
                "SELECT * FROM user WHERE name=? AND password=?", name, password);
        return row == null ? null : UserFactory.fromMap(row);
    }

    /** 模糊查询，按创建时间倒序（最新注册的在最前） */
    public List<User> list(String keyword) {
        String sql = "SELECT * FROM user";
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " WHERE name LIKE ? OR phone LIKE ? OR role LIKE ? OR major LIKE ?";
            String k = "%" + keyword.trim() + "%";
            params.add(k); params.add(k); params.add(k); params.add(k);
        }
        sql += " ORDER BY createTime DESC";
        List<User> users = new ArrayList<>();
        for (Map<String, Object> row : JDBCTool.queryMaps(sql, params.toArray())) {
            users.add(UserFactory.fromMap(row));
        }
        return users;
    }

    public void save(User user) {
        try (Connection c = JDBCTool.getConnection()) {
            JDBCTool.save(user, c);
        } catch (Exception e) {
            throw new BizException("保存用户失败：" + e.getMessage());
        }
    }

    public void updateLoginTime(String id) {
        LocalDateTime now = LocalDateTime.now();
        JDBCTool.execute("UPDATE user SET loginTime=?, updateTime=? WHERE id=?", now, now, id);
    }

    public List<Map<String, Object>> allMajors() {
        return JDBCTool.queryMaps("SELECT DISTINCT major FROM user WHERE major IS NOT NULL ORDER BY major");
    }

    /** 修改密码：校验原密码后更新为新密码 */
    public void changePassword(String id, String oldPassword, String newPassword) {
        User user = findById(id);
        if (user == null) throw new BizException("用户不存在");
        if (user.getPassword() == null || !user.getPassword().equals(oldPassword)) {
            throw new BizException("原密码不正确");
        }
        JDBCTool.execute("UPDATE user SET password=?, updateTime=? WHERE id=?",
                newPassword, LocalDateTime.now(), id);
    }
}
