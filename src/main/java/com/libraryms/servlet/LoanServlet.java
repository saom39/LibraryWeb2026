package com.libraryms.servlet;

import com.libraryms.dao.LoanDao;
import com.libraryms.util.BizException;
import com.libraryms.util.Result;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 借书 / 还书 / 借阅记录查询 / 个人借阅查询 */
@WebServlet("/api/loans")
public class LoanServlet extends BaseServlet {

    private final LoanDao loanDao = new LoanDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            String scope = param(req, "scope");
            if ("query".equals(scope)) {
                requireRole(req, UserFactory.ADMIN);
                return Result.ok(loanDao.query(param(req, "mode"), param(req, "keyword"),
                        param(req, "start"), param(req, "end")));
            }
            // 默认：个人借阅查询（all / unreturned / range）
            String userId = currentUserId(req);
            return Result.ok(loanDao.mine(userId, param(req, "mode"),
                    param(req, "start"), param(req, "end")));
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            String action = param(req, "action");
            String userId = currentUserId(req);
            if ("borrow".equals(action)) {
                String bookId = param(req, "bookId");
                if (bookId == null || bookId.isEmpty()) throw new BizException("请选择/输入要借阅的图书 id");
                return Result.ok("借书成功", loanDao.borrow(userId, bookId));
            } else if ("return".equals(action)) {
                String copyId = param(req, "copyId");
                if (copyId == null || copyId.isEmpty()) throw new BizException("请选择/输入要归还的副本 id");
                return Result.ok("还书成功", loanDao.returnBook(userId, copyId));
            }
            throw new BizException("未知的借阅操作");
        });
    }
}
