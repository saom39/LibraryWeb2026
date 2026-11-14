package com.libraryms.dao;

import com.libraryms.entity.Book;
import com.libraryms.util.BizException;
import com.libraryms.util.IdUtil;
import com.libraryms.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 图书数据访问 */
public class BookDao {

    private static final String COLUMNS = "b.id,b.createTime,b.updateTime,b.title,b.author,b.type," +
            "b.publisher,b.days,b.price,b.stock,b.status,b.introduction";

    /** 模糊查询；关键字为空时查询全部；新添加的图书排第一行 */
    public List<Book> list(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(", (SELECT COUNT(*) FROM bookcopy c WHERE c.bookId=b.id AND c.isBorrowed=0) AS availableCount")
                .append(" FROM book b");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" WHERE b.title LIKE ? OR b.author LIKE ? OR b.type LIKE ? OR b.publisher LIKE ?");
            String k = "%" + keyword.trim() + "%";
            params.add(k); params.add(k); params.add(k); params.add(k);
        }
        sql.append(" ORDER BY b.createTime DESC, b.id DESC");
        return JDBCTool.queryList(sql.toString(), Book.class, params.toArray());
    }

    public Book findById(String id) {
        try (Connection c = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, Book.class, c);
        } catch (Exception e) {
            throw new BizException("查询图书失败：" + e.getMessage());
        }
    }

    public Book findById(String id, Connection c) {
        return JDBCTool.getOneById(id, Book.class, c);
    }

    /** 添加图书，并按 stock 自动生成副本（同一事务） */
    public void add(Book book, int stock) {
        Connection c = null;
        try {
            c = JDBCTool.getConnection();
            c.setAutoCommit(false);
            LocalDateTime now = LocalDateTime.now();
            book.setId(IdUtil.uuid());
            book.setCreateTime(now);
            book.setUpdateTime(now);
            book.setStock(stock);
            if (book.getStatus() == null || book.getStatus().isEmpty()) book.setStatus("正常");
            if (book.getDays() == null || book.getDays() <= 0) book.setDays(30);
            JDBCTool.save(book, c);                       // 保存图书主记录
            for (int i = 0; i < stock; i++) {
                BookCopyDao.addCopy(book.getId(), c);
            }
            c.commit();
        } catch (Exception e) {
            rollback(c);
            throw new BizException("添加图书失败：" + e.getMessage());
        } finally {
            JDBCTool.close(c);
        }
    }

    /** 编辑图书：只更新 title/author/type/publisher/days/price，不动 stock/status/副本 */
    public void updateEditable(Book book) {
        if (book.getId() == null || book.getId().isEmpty()) throw new BizException("缺少图书 id");
        JDBCTool.execute("UPDATE book SET title=?,author=?,type=?,publisher=?,days=?,price=?,updateTime=? WHERE id=?",
                book.getTitle(), book.getAuthor(), book.getType(), book.getPublisher(),
                book.getDays(), book.getPrice(), LocalDateTime.now(), book.getId());
    }

    /** 废弃图书：全部副本归还（可借副本数 == 库存）才允许 */
    public void discard(String id) {
        Book book = findById(id);
        if (book == null) throw new BizException("图书不存在");
        if ("已废弃".equals(book.getStatus())) throw new BizException("该图书已废弃");
        int borrowed = intScalar("SELECT COUNT(*) FROM bookcopy WHERE bookId=? AND isBorrowed=1", id);
        int available = intScalar("SELECT COUNT(*) FROM bookcopy WHERE bookId=? AND isBorrowed=0", id);
        int stock = book.getStock() == null ? 0 : book.getStock();
        if (borrowed > 0) throw new BizException("还有 " + borrowed + " 本副本外借未还，不能废弃图书");
        if (available != stock) throw new BizException("可借副本数(" + available + ")与库存(" + stock + ")不一致，不能废弃");
        JDBCTool.execute("UPDATE book SET status='已废弃', updateTime=? WHERE id=?", LocalDateTime.now(), id);
    }

    public void changeStock(String id, int delta) {
        JDBCTool.execute("UPDATE book SET stock=stock+?, updateTime=? WHERE id=?",
                delta, LocalDateTime.now(), id);
    }

    private int intScalar(String sql, Object... params) {
        Object v = JDBCTool.queryScalar(sql, params);
        return v == null ? 0 : ((Number) v).intValue();
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (Exception ignored) { }
    }
}
