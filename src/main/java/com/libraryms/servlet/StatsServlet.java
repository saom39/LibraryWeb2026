package com.libraryms.servlet;

import com.libraryms.dao.LoanDao;
import com.libraryms.util.Result;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 借阅数据统计：按图书类型 / 按用户专业 */
@WebServlet("/api/stats")
public class StatsServlet extends BaseServlet {

    private final LoanDao loanDao = new LoanDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            requireRole(req, UserFactory.ADMIN);
            if ("major".equals(param(req, "type"))) {
                return Result.ok(loanDao.statsByMajor());
            }
            return Result.ok(loanDao.statsByType());
        });
    }
}
