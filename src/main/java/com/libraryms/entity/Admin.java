package com.libraryms.entity;

import com.libraryms.annotation.Column;

import java.time.LocalDate;

/** 管理员 */
public class Admin extends User {
    /** 权限等级 */
    @Column("permissionLevel")
    private Integer permissionLevel;
    /** 入职时间 */
    @Column("entryTime")
    private LocalDate entryTime;

    @Override
    public String[] hasPermission() {
        return new String[]{"user:query", "loan:query", "stats", "borrow", "return", "recommend:review"};
    }

    public Integer getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
    public LocalDate getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDate entryTime) { this.entryTime = entryTime; }
}
