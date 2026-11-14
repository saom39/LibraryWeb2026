package com.libraryms.entity;

import com.libraryms.annotation.Column;

/** 教师 */
public class Teacher extends User {
    /** 所属学院 */
    @Column("college")
    private String college;
    /** 职称 */
    @Column("title")
    private String title;

    @Override
    public String[] hasPermission() {
        return new String[]{"borrow", "return", "loan:mine", "recommend:add"};
    }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
