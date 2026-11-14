package com.libraryms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.libraryms.annotation.Column;
import com.libraryms.annotation.Table;

import java.time.LocalDateTime;

/** 借阅记录 */
@Table("loan")
public class Loan extends Base {
    /** 图书副本（对象引用，不参与 ORM 映射） */
    @JsonIgnore
    private BookCopy bookCopy;
    /** 用户（对象引用，不参与 ORM 映射） */
    @JsonIgnore
    private User user;
    /** 副本 id */
    @Column("bookCopyId")
    private String bookCopyId;
    /** 用户 id */
    @Column("userId")
    private String userId;
    /** 借书日期 */
    @Column("borrowDate")
    private LocalDateTime borrowDate;
    /** 还书日期 */
    @Column("returnDate")
    private LocalDateTime returnDate;
    /** 应还日期（根据图书 days 计算） */
    @Column("dueDate")
    private LocalDateTime dueDate;
    /** 状态：未还、已还、超期 */
    @Column("status")
    private String status;
    /** 展示用：书名、用户名 */
    private String bookTitle;
    private String userName;

    public BookCopy getBookCopy() { return bookCopy; }
    public void setBookCopy(BookCopy bookCopy) { this.bookCopy = bookCopy; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getBookCopyId() { return bookCopyId; }
    public void setBookCopyId(String bookCopyId) { this.bookCopyId = bookCopyId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
