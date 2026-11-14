package com.libraryms.dao;

import com.libraryms.entity.BookCopy;
import com.libraryms.util.BizException;
import com.libraryms.util.IdUtil;
import com.libraryms.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 图书副本数据访问 */
public class BookCopyDao {

    public List<BookCopy> listByBook(String bookId) {
        String sql = "SELECT c.*, b.title AS bookTitle FROM bookcopy c LEFT JOIN book b ON c.bookId=b.id";
        List<Object> params = new ArrayList<>();
        if (bookId != null && !bookId.isEmpty()) {
            sql += " WHERE c.bookId=?";
            params.add(bookId);
        }
        sql += " ORDER BY c.createTime DESC, c.id DESC";
        return JDBCTool.queryList(sql, BookCopy.class, params.toArray());
    }

    public BookCopy findById(String id) {
        try (Connection c = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, BookCopy.class, c);
        } catch (Exception e) {
            throw new BizException("查询副本失败：" + e.getMessage());
        }
    }

    /** 新增一个副本（由调用方在同一事务里同时更新 book.stock） */
    public static void addCopy(String bookId, Connection c) {
        BookCopy copy = new BookCopy();
        copy.setId(IdUtil.uuid());
        LocalDateTime now = LocalDateTime.now();
        copy.setCreateTime(now);
        copy.setUpdateTime(now);
        copy.setBookId(bookId);
        copy.setBorrowed(false);
        JDBCTool.save(copy, c);
    }

    /** 副本新增：新增副本 + 库存加 1 */
    public void add(String bookId) {
        Connection c = null;
        try {
            c = JDBCTool.getConnection();
            c.setAutoCommit(false);
            addCopy(bookId, c);
            JDBCTool.execute("UPDATE book SET stock=stock+1, updateTime=? WHERE id=?", c,
                    LocalDateTime.now(), bookId);
            c.commit();
        } catch (Exception e) {
            rollback(c);
            throw new BizException("新增副本失败：" + e.getMessage());
        } finally {
            JDBCTool.close(c);
        }
    }

    /** 副本删除：借阅状态下不能删除，删除后库存减 1 */
    public void delete(String copyId) {
        Connection c = null;
        try {
            c = JDBCTool.getConnection();
            c.setAutoCommit(false);
            BookCopy copy = JDBCTool.getOneById(copyId, BookCopy.class, c);
            if (copy == null) throw new BizException("副本不存在");
            if (copy.isBorrowed()) throw new BizException("该副本处于借阅状态，不能删除");
            JDBCTool.delete(copy, c);                 // 删除副本记录
            JDBCTool.execute("UPDATE book SET stock=stock-1, updateTime=? WHERE id=?", c,
                    LocalDateTime.now(), copy.getBookId());
            c.commit();
        } catch (BizException e) {
            rollback(c);
            throw e;
        } catch (Exception e) {
            rollback(c);
            throw new BizException("删除副本失败：" + e.getMessage());
        } finally {
            JDBCTool.close(c);
        }
    }

    /** 找一本可借副本（图书必须未废弃） */
    public BookCopy findAvailable(String bookId, Connection c) {
        String sql = "SELECT cp.* FROM bookcopy cp JOIN book b ON cp.bookId=b.id " +
                "WHERE cp.bookId=? AND cp.isBorrowed=0 AND b.status='正常' ORDER BY cp.createTime LIMIT 1";
        List<BookCopy> list = JDBCTool.queryList(sql, c, BookCopy.class, bookId);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void setBorrowed(String copyId, boolean borrowed, Connection c) {
        JDBCTool.execute("UPDATE bookcopy SET isBorrowed=?, updateTime=? WHERE id=?", c,
                borrowed, LocalDateTime.now(), copyId);
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (Exception ignored) { }
    }
}
