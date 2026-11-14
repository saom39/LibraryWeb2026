package com.libraryms.servlet;

import com.libraryms.dao.UserDao;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/** 用户信息查询（管理员专属，只能查询，不能修改/新增/删除） */
@WebServlet("/api/users")
public class UserServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            requireRole(req, UserFactory.ADMIN);
            String keyword = param(req, "keyword");
            // 默认不显示用户数据，只有输入关键字查询后才返回
            if (keyword == null || keyword.trim().isEmpty()) {
                return com.libraryms.util.Result.ok(new ArrayList<>());
            }
            return com.libraryms.util.Result.ok(userDao.list(keyword));
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        fail(resp, "用户信息只能查询，不能修改、新增或删除");
    }
}
