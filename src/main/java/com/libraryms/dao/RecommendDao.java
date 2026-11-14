package com.libraryms.dao;

import com.libraryms.entity.Book;
import com.libraryms.entity.BookRecommend;
import com.libraryms.util.BizException;
import com.libraryms.util.IdUtil;
import com.libraryms.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 推荐图书 / 审阅推荐 */
public class RecommendDao {

    public List<Map<String, Object>> list(String status) {
        String sql = "SELECT r.*, u.name AS userName FROM bookrecommend r " +
                "LEFT JOIN user u ON r.userId=u.id";
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty() && !"all".equals(status)) {
            sql += " WHERE r.status=?";
            params.add(status);
        }
        sql += " ORDER BY r.createTime DESC";
        return JDBCTool.queryMaps(sql, params.toArray());
    }

    public BookRecommend findById(String id) {
        try (Connection c = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, BookRecommend.class, c);
        } catch (Exception e) {
            throw new BizException("查询推荐失败：" + e.getMessage());
        }
    }

    /** 教师提交推荐（若馆内已有相关书籍，由 Servlet 直接返回，不落库） */
    public void save(BookRecommend recommend) {
        try (Connection c = JDBCTool.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            recommend.setId(IdUtil.uuid());
            recommend.setCreateTime(now);
            recommend.setUpdateTime(now);
            recommend.setStatus("待审阅");
            JDBCTool.save(recommend, c);
        } catch (Exception e) {
            throw new BizException("提交推荐失败：" + e.getMessage());
        }
    }

    /** 审阅通过：加入图书列表并自动生成一个副本 */
    public Map<String, Object> approve(String id) {
        Connection c = null;
        try {
            c = JDBCTool.getConnection();
            c.setAutoCommit(false);
            BookRecommend r = JDBCTool.getOneById(id, BookRecommend.class, c);
            if (r == null) throw new BizException("推荐记录不存在");
            if (!"待审阅".equals(r.getStatus())) throw new BizException("该推荐已处理，不能重复审阅");

            LocalDateTime now = LocalDateTime.now();
            Book book = new Book();
            book.setId(IdUtil.uuid());
            book.setCreateTime(now);
            book.setUpdateTime(now);
            book.setTitle(r.getBookName());
            book.setAuthor(r.getAuthor());
            book.setType("推荐图书");
            book.setPublisher(r.getPublisher());
            book.setDays(30);
            book.setPrice(r.getPrice());
            book.setStock(1);
            book.setStatus("正常");
            book.setIntroduction("由读者 " + r.getUserId() + " 推荐，经管理员审阅入库");
            JDBCTool.save(book, c);
            BookCopyDao.addCopy(book.getId(), c);
            JDBCTool.execute("UPDATE bookrecommend SET status='已入库', updateTime=? WHERE id=?", c, now, id);
            c.commit();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("bookId", book.getId());
            data.put("title", book.getTitle());
            return data;
        } catch (BizException e) {
            rollback(c);
            throw e;
        } catch (Exception e) {
            rollback(c);
            throw new BizException("审阅通过失败：" + e.getMessage());
        } finally {
            JDBCTool.close(c);
        }
    }

    /** 驳回推荐并记录理由 */
    public void reject(String id, String reason) {
        if (reason == null || reason.trim().isEmpty()) throw new BizException("请填写驳回理由");
        BookRecommend r = findById(id);
        if (r == null) throw new BizException("推荐记录不存在");
        if (!"待审阅".equals(r.getStatus())) throw new BizException("该推荐已处理，不能重复审阅");
        JDBCTool.execute("UPDATE bookrecommend SET status='已驳回', reason=?, updateTime=? WHERE id=?",
                reason, LocalDateTime.now(), id);
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (Exception ignored) { }
    }
}
