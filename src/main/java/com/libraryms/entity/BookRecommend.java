package com.libraryms.entity;

import com.libraryms.annotation.Column;
import com.libraryms.annotation.Table;

/** 推荐图书 */
@Table("bookrecommend")
public class BookRecommend extends Base {
    @Column("userId")
    private String userId;
    @Column("bookName")
    private String bookName;
    @Column("author")
    private String author;
    @Column("publisher")
    private String publisher;
    @Column("price")
    private Double price;
    /** 待审阅 / 已入库 / 已驳回 */
    @Column("status")
    private String status;
    /** 驳回理由 */
    @Column("reason")
    private String reason;
    /** 展示用：推荐人 */
    private String userName;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
