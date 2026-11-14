package com.libraryms.dao;

import com.libraryms.entity.Book;
import com.libraryms.entity.BookCopy;
import com.libraryms.entity.Loan;
import com.libraryms.util.BizException;
import com.libraryms.util.IdUtil;
import com.libraryms.util.JDBCTool;

import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 借阅记录数据访问（借书、还书、查询、统计） */
public class LoanDao {

    private static final String VIEW =
            "SELECT l.id,l.bookCopyId,l.userId,l.borrowDate,l.returnDate,l.dueDate,l.status," +
            "u.name AS userName,u.major AS userMajor,b.title AS bookTitle,b.type AS bookType," +
            "b.id AS bookId FROM loan l " +
            "LEFT JOIN user u ON l.userId=u.id " +
            "LEFT JOIN bookcopy c ON l.bookCopyId=c.id " +
            "LEFT JOIN book b ON c.bookId=b.id ";

    private final BookDao bookDao = new BookDao();
    private final BookCopyDao copyDao = new BookCopyDao();

    /** 借书：事务内生成借阅记录并把副本置为已借出 */
    public Map<String, Object> borrow(String userId, String bookId) {
        Connection c = null;
        try {
            c = JDBCTool.getConnection();
            c.setAutoCommit(false);
            Book book = bookDao.findById(bookId, c);
            if (book == null) throw new BizException("图书不存在，请检查图书 id");
            if (!"正常".equals(book.getStatus())) throw new BizException("该图书已废弃，不能借阅");
            BookCopy copy = copyDao.findAvailable(bookId, c);
            if (copy == null) throw new BizException("该图书已无可借副本");

            LocalDateTime now = LocalDateTime.now();
            int days = book.getDays() == null || book.getDays() <= 0 ? 30 : book.getDays();
            Loan loan = new Loan();
            loan.setId(IdUtil.uuid());
            loan.setCreateTime(now);
            loan.setUpdateTime(now);
            loan.setUserId(userId);
            loan.setBookCopyId(copy.getId());
            loan.setBorrowDate(now);
            loan.setDueDate(now.plusDays(days));
            loan.setStatus("未还");
            JDBCTool.save(loan, c);                                  // 保存借阅记录
            BookCopyDao.setBorrowed(copy.getId(), true, c);
            c.commit();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("loanId", loan.getId());
            data.put("bookId", bookId);
            data.put("bookTitle", book.getTitle());
            data.put("bookCopyId", copy.getId());
            data.put("dueDate", loan.getDueDate());
            return data;
        } catch (BizException e) {
            rollback(c);
            throw e;
        } catch (Exception e) {
            rollback(c);
            throw new BizException("借书失败：" + e.getMessage());
        } finally {
            JDBCTool.close(c);
        }
    }

    /** 还书：按副本 id 归还，事务内释放副本 */
    public Map<String, Object> returnBook(String userId, String copyId) {
        Connection c = null;
        try {
            c = JDBCTool.getConnection();
            c.setAutoCommit(false);
            String sql = "SELECT * FROM loan WHERE bookCopyId=? AND userId=? AND returnDate IS NULL " +
                    "ORDER BY borrowDate DESC";
            List<Loan> loans = JDBCTool.queryList(sql, c, Loan.class, copyId, userId);
            if (loans.isEmpty()) throw new BizException("未找到该副本的未还借阅记录，请检查副本 id");
            Loan loan = loans.get(0);
            LocalDateTime now = LocalDateTime.now();
            String status = (loan.getDueDate() != null && now.isAfter(loan.getDueDate())) ? "超期" : "已还";
            JDBCTool.execute("UPDATE loan SET returnDate=?, status=?, updateTime=? WHERE id=?", c,
                    now, status, now, loan.getId());
            BookCopyDao.setBorrowed(copyId, false, c);
            c.commit();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("loanId", loan.getId());
            data.put("bookCopyId", copyId);
            data.put("status", status);
            data.put("returnDate", now);
            return data;
        } catch (BizException e) {
            rollback(c);
            throw e;
        } catch (Exception e) {
            rollback(c);
            throw new BizException("还书失败：" + e.getMessage());
        } finally {
            JDBCTool.close(c);
        }
    }

    /** 管理员借阅记录查询：all / keyword / overdue / range */
    public List<Map<String, Object>> query(String mode, String keyword, String start, String end) {
        StringBuilder sql = new StringBuilder(VIEW);
        List<Object> params = new ArrayList<>();
        String m = mode == null ? "all" : mode;
        if ("keyword".equals(m)) {
            sql.append(" WHERE (u.name LIKE ? OR b.title LIKE ?)");
            String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            params.add(k); params.add(k);
        } else if ("overdue".equals(m)) {
            sql.append(" WHERE l.returnDate IS NULL AND l.dueDate < NOW()");
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND (u.name LIKE ? OR b.title LIKE ?)");
                String k = "%" + keyword.trim() + "%";
                params.add(k); params.add(k);
            }
        } else if ("range".equals(m)) {
            sql.append(" WHERE l.borrowDate >= ? AND l.borrowDate <= ?");
            params.add(dateStart(start)); params.add(dateEnd(end));
        }
        sql.append(" ORDER BY l.borrowDate DESC");
        List<Map<String, Object>> rows = JDBCTool.queryMaps(sql.toString(), params.toArray());
        attachFine(rows);
        return rows;
    }

    /** 个人借阅查询：all / unreturned / range */
    public List<Map<String, Object>> mine(String userId, String mode, String start, String end) {
        StringBuilder sql = new StringBuilder(VIEW).append(" WHERE l.userId=?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        String m = mode == null ? "all" : mode;
        if ("unreturned".equals(m)) {
            sql.append(" AND l.returnDate IS NULL");
        } else if ("range".equals(m)) {
            sql.append(" AND l.borrowDate >= ? AND l.borrowDate <= ?");
            params.add(dateStart(start)); params.add(dateEnd(end));
        }
        sql.append(" ORDER BY l.borrowDate DESC");
        List<Map<String, Object>> rows = JDBCTool.queryMaps(sql.toString(), params.toArray());
        attachFine(rows);
        return rows;
    }

    /** 按图书类型统计借阅次数（所有类型都显示，无借阅的记 0） */
    public List<Map<String, Object>> statsByType() {
        List<Map<String, Object>> types = JDBCTool.queryMaps(
                "SELECT DISTINCT type AS name FROM book WHERE type IS NOT NULL ORDER BY type");
        List<Map<String, Object>> counts = JDBCTool.queryMaps(
                "SELECT b.type AS name, COUNT(*) AS total FROM loan l " +
                "JOIN bookcopy c ON l.bookCopyId=c.id JOIN book b ON c.bookId=b.id " +
                "WHERE b.type IS NOT NULL GROUP BY b.type");
        return merge(types, counts);
    }

    /** 按用户专业统计借阅次数 */
    public List<Map<String, Object>> statsByMajor() {
        List<Map<String, Object>> majors = JDBCTool.queryMaps(
                "SELECT DISTINCT major AS name FROM user WHERE major IS NOT NULL ORDER BY major");
        List<Map<String, Object>> counts = JDBCTool.queryMaps(
                "SELECT u.major AS name, COUNT(*) AS total FROM loan l " +
                "JOIN user u ON l.userId=u.id WHERE u.major IS NOT NULL GROUP BY u.major");
        return merge(majors, counts);
    }

    private List<Map<String, Object>> merge(List<Map<String, Object>> keys, List<Map<String, Object>> counts) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map<String, Object> k : keys) {
            Object name = k.get("name");
            if (name != null) map.put(name.toString(), 0L);
        }
        for (Map<String, Object> row : counts) {
            Object name = row.get("name");
            Object total = row.get("total");
            if (name != null) map.put(name.toString(), total == null ? 0L : ((Number) total).longValue());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : map.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", e.getKey());
            row.put("total", e.getValue());
            result.add(row);
        }
        return result;
    }

    /** 计算并填充每条借阅记录的逾期罚金（逾期每天 1.5 元） */
    private void attachFine(List<Map<String, Object>> rows) {
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> row : rows) {
            double fine = 0;
            Object dueObj = row.get("dueDate");
            if (dueObj instanceof LocalDateTime) {
                LocalDateTime due = (LocalDateTime) dueObj;
                Object retObj = row.get("returnDate");
                LocalDateTime end = (retObj instanceof LocalDateTime) ? (LocalDateTime) retObj : now;
                long days = Duration.between(due, end).toDays();
                if (days > 0) fine = days * 1.5;
            }
            row.put("fine", fine);
        }
    }

    private String dateStart(String s) {
        return (s == null || s.trim().isEmpty()) ? "1970-01-01 00:00:00" : s.trim() + " 00:00:00";
    }

    private String dateEnd(String s) {
        return (s == null || s.trim().isEmpty()) ? "2099-12-31 23:59:59" : s.trim() + " 23:59:59";
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (Exception ignored) { }
    }
}
