package com.libraryms.entity;

import com.libraryms.annotation.Column;

/** 操作员 */
public class Operator extends User {
    /** 负责区域 */
    @Column("area")
    private String area;
    /** 职务 */
    @Column("duty")
    private String duty;

    @Override
    public String[] hasPermission() {
        return new String[]{"book:add", "book:list", "book:discard", "book:edit",
                "book:copy", "borrow", "return", "loan:mine"};
    }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getDuty() { return duty; }
    public void setDuty(String duty) { this.duty = duty; }
}
