package com.libraryms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.libraryms.annotation.Column;
import com.libraryms.annotation.Table;

/** 图书副本 */
@Table("bookcopy")
public class BookCopy extends Base {
    /** 属于哪种书（对象引用，不参与 ORM 映射） */
    @JsonIgnore
    private Book book;
    /** 外键：图书 id */
    @Column("bookId")
    private String bookId;
    /** 借阅状态，true 为已借出，false 为未借阅 */
    @Column("isBorrowed")
    private boolean isBorrowed;
    /** 书名（列表展示用） */
    private String bookTitle;

    public Book getBook() { return book; }
    public void setBook(Book book) {
        this.book = book;
        if (book != null) this.bookId = book.getId();
    }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public boolean isBorrowed() { return isBorrowed; }
    public void setBorrowed(boolean borrowed) { this.isBorrowed = borrowed; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
}
