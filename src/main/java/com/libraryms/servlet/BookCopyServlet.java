package com.libraryms.servlet;

import com.libraryms.dao.BookCopyDao;
import com.libraryms.util.BizException;
import com.libraryms.util.Result;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 图书副本编辑：新增（库存 +1）/ 删除（库存 -1，借阅中不可删） */
@WebServlet("/api/copies")
public class BookCopyServlet extends BaseServlet {

    private final BookCopyDao copyDao = new BookCopyDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> Result.ok(copyDao.listByBook(param(req, "bookId"))));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            requireRole(req, UserFactory.OPERATOR);
            String action = param(req, "action");
            String bookId = param(req, "bookId");
            if ("add".equals(action)) {
                if (bookId == null || bookId.isEmpty()) throw new BizException("缺少图书 id");
                copyDao.add(bookId);
                return Result.ok("副本已新增，库存 +1", copyDao.listByBook(bookId));
            } else if ("delete".equals(action)) {
                String copyId = param(req, "copyId");
                if (copyId == null || copyId.isEmpty()) throw new BizException("缺少副本 id");
                copyDao.delete(copyId);
                return Result.ok("副本已删除，库存 -1", copyDao.listByBook(bookId));
            }
            throw new BizException("未知的副本操作");
        });
    }
}
