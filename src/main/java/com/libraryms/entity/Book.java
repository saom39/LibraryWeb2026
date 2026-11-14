package com.libraryms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.libraryms.annotation.Column;
import com.libraryms.annotation.Table;

import java.util.List;

/** 图书 */
@Table("book")
public class Book extends Base {
    /** 书名 */
    @Column("title")
    private String title;
    /** 作者 */
    @Column("author")
    private String author;
    /** 类型 */
    @Column("type")
    private String type;
    /** 出版社 */
    @Column("publisher")
    private String publisher;
    /** 还书期限天数 */
    @Column("days")
    private Integer days;
    /** 价格 */
    @Column("price")
    private Double price;
    /** 库存 */
    @Column("stock")
    private Integer stock;
    /** 图书状态：正常、已废弃 */
    @Column("status")
    private String status;
    /** 图书简介 */
    @Column("introduction")
    private String introduction;
    /** 图书副本集合（不参与 ORM 映射） */
    @JsonIgnore
    private List<BookCopy> bookCopies;
    /** 可借副本数（列表查询时由 SQL 统计得出） */
    private Integer availableCount;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public List<BookCopy> getBookCopies() { return bookCopies; }
    public void setBookCopies(List<BookCopy> bookCopies) { this.bookCopies = bookCopies; }
    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer availableCount) { this.availableCount = availableCount; }
}
