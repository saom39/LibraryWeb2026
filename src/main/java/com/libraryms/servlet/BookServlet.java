package com.libraryms.servlet;

import com.libraryms.dao.BookDao;
import com.libraryms.entity.Book;
import com.libraryms.util.BizException;
import com.libraryms.util.Result;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 图书：查询 / 添加 / 编辑 / 废弃 */
@WebServlet("/api/books")
public class BookServlet extends BaseServlet {

    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> Result.ok(bookDao.list(param(req, "keyword"))));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = param(req, "action");
        if ("add".equals(action)) {
            run(resp, () -> {
                requireRole(req, UserFactory.OPERATOR);
                Book book = new Book();
                book.setTitle(param(req, "title"));
                book.setAuthor(param(req, "author"));
                book.setType(param(req, "type"));
                book.setPublisher(param(req, "publisher"));
                book.setDays(intParam(req, "days", 30));
                book.setPrice(doubleParam(req, "price", 0));
                book.setIntroduction(param(req, "introduction"));
                if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                    throw new BizException("书名不能为空");
                }
                int stock = intParam(req, "stock", 1);
                if (stock < 1) throw new BizException("库存至少为 1");
                bookDao.add(book, stock);
                return Result.ok("添加成功，已自动生成 " + stock + " 本副本", bookDao.list(""));
            });
        } else if ("edit".equals(action)) {
            run(resp, () -> {
                requireRole(req, UserFactory.OPERATOR);
                Book book = new Book();
                book.setId(param(req, "id"));
                book.setTitle(param(req, "title"));
                book.setAuthor(param(req, "author"));
                book.setType(param(req, "type"));
                book.setPublisher(param(req, "publisher"));
                book.setDays(intParam(req, "days", 30));
                book.setPrice(doubleParam(req, "price", 0));
                bookDao.updateEditable(book);
                return Result.ok("图书信息已更新", bookDao.list(""));
            });
        } else if ("discard".equals(action)) {
            run(resp, () -> {
                requireRole(req, UserFactory.OPERATOR);
                String id = param(req, "id");
                bookDao.discard(id);
                return Result.ok("图书已废弃", bookDao.list(""));
            });
        } else {
            fail(resp, "未知的图书操作");
        }
    }
}
