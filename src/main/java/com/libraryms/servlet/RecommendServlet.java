package com.libraryms.servlet;

import com.libraryms.dao.BookDao;
import com.libraryms.dao.RecommendDao;
import com.libraryms.entity.Book;
import com.libraryms.entity.BookRecommend;
import com.libraryms.util.BizException;
import com.libraryms.util.Result;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 推荐图书（教师）/ 审阅推荐（管理员） */
@WebServlet("/api/recommends")
public class RecommendServlet extends BaseServlet {

    private final RecommendDao recommendDao = new RecommendDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            List<Map<String, Object>> list = recommendDao.list(param(req, "status"));
            if ("1".equals(param(req, "mine"))) {
                String userId = currentUserId(req);
                List<Map<String, Object>> mine = new ArrayList<>();
                for (Map<String, Object> row : list) {
                    if (userId.equals(String.valueOf(row.get("userId")))) mine.add(row);
                }
                return Result.ok(mine);
            }
            return Result.ok(list);
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            String action = param(req, "action");
            if ("add".equals(action)) {
                requireRole(req, UserFactory.TEACHER);
                String bookName = param(req, "bookName");
                if (bookName == null || bookName.trim().isEmpty()) throw new BizException("请输入书名");
                // 馆内已有相关书籍 → 直接返回已有书籍信息，不进入审核
                List<Book> similar = bookDao.list(bookName.trim());
                if (!similar.isEmpty()) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("submitted", false);
                    data.put("books", similar);
                    return Result.ok("图书馆已有相关书籍，无需推荐", data);
                }
                BookRecommend recommend = new BookRecommend();
                recommend.setUserId(currentUserId(req));
                recommend.setBookName(bookName.trim());
                recommend.setAuthor(param(req, "author"));
                recommend.setPublisher(param(req, "publisher"));
                recommend.setPrice(doubleParam(req, "price", 0));
                recommendDao.save(recommend);
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("submitted", true);
                return Result.ok("馆内暂无此书，已自动提交到图书审核", data);
            } else if ("approve".equals(action)) {
                requireRole(req, UserFactory.ADMIN);
                return Result.ok("已加入图书列表", recommendDao.approve(param(req, "id")));
            } else if ("reject".equals(action)) {
                requireRole(req, UserFactory.ADMIN);
                recommendDao.reject(param(req, "id"), param(req, "reason"));
                return Result.ok("已驳回推荐", recommendDao.list("all"));
            }
            throw new BizException("未知的推荐操作");
        });
    }
}
