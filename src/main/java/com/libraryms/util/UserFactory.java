package com.libraryms.util;

import com.libraryms.entity.Admin;
import com.libraryms.entity.Operator;
import com.libraryms.entity.Student;
import com.libraryms.entity.Teacher;
import com.libraryms.entity.User;

import java.util.Map;

/** 根据 role 构造 User 的具体子类（单表继承） */
public class UserFactory {

    public static final String ADMIN = "管理员";
    public static final String OPERATOR = "操作员";
    public static final String STUDENT = "学生";
    public static final String TEACHER = "教师";

    /** 兼容前端传英文 role */
    public static String normalize(String role) {
        if (role == null) return STUDENT;
        switch (role.toLowerCase()) {
            case "admin": return ADMIN;
            case "operator": return OPERATOR;
            case "teacher": return TEACHER;
            case "student": return STUDENT;
            default: return role;
        }
    }

    public static Class<? extends User> classOf(String role) {
        String r = normalize(role);
        if (ADMIN.equals(r)) return Admin.class;
        if (OPERATOR.equals(r)) return Operator.class;
        if (TEACHER.equals(r)) return Teacher.class;
        return Student.class;
    }

    public static User fromMap(Map<String, Object> row) {
        Object role = row.get("role");
        return EntityMapper.mapToEntity(row, classOf(role == null ? null : role.toString()));
    }

    public static User create(String role) {
        Class<? extends User> c = classOf(role);
        try {
            return c.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建用户对象失败", e);
        }
    }
}
