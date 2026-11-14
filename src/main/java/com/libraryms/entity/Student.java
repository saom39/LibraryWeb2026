package com.libraryms.entity;

import com.libraryms.annotation.Column;

/** 学生 */
public class Student extends User {
    /** 年级 */
    @Column("grade")
    private String grade;
    /** 所属学院 */
    @Column("college")
    private String college;

    @Override
    public String[] hasPermission() {
        return new String[]{"borrow", "return", "loan:mine"};
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
}
