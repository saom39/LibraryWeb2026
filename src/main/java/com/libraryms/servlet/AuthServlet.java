package com.libraryms.servlet;

import com.libraryms.dao.UserDao;
import com.libraryms.entity.User;
import com.libraryms.util.BizException;
import com.libraryms.util.Result;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 登录 / 退出 / 当前登录用户（含角色权限列表） */
@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/current".equals(path)) {
            run(resp, () -> {
                User u = currentUser(req);
                if (u == null) return Result.fail("未登录");
                return Result.ok(userView(u));
            });
        } else {
            fail(resp, "未知请求");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/login".equals(path)) {
            login(req, resp);
        } else if ("/logout".equals(path)) {
            req.getSession().invalidate();
            ok(resp, "已退出登录", null);
        } else if ("/password".equals(path)) {
            changePassword(req, resp);
        } else {
            fail(resp, "未知请求");
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            String name = param(req, "name");
            String password = param(req, "password");
            String captcha = param(req, "captcha");
            if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                throw new BizException("请输入用户名和密码");
            }
            Object expected = req.getSession().getAttribute("captcha");
            if (expected == null || captcha == null
                    || !expected.toString().equalsIgnoreCase(captcha.trim())) {
                throw new BizException("验证码错误，请重新输入");
            }
            req.getSession().removeAttribute("captcha");
            User user = userDao.login(name.trim(), password);
            if (user == null) throw new BizException("用户名或密码错误");
            userDao.updateLoginTime(user.getId());
            user.setLoginTime(LocalDateTime.now());
            req.getSession().setAttribute("user", user);
            return Result.ok("登录成功", userView(user));
        });
    }

    private void changePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            User u = currentUser(req);
            if (u == null) throw new BizException("请先登录");
            String oldPassword = param(req, "oldPassword");
            String newPassword = param(req, "newPassword");
            if (oldPassword == null || oldPassword.trim().isEmpty()) throw new BizException("请输入原密码");
            if (newPassword == null || newPassword.trim().isEmpty()) throw new BizException("请输入新密码");
            if (newPassword.trim().length() < 6) throw new BizException("新密码长度至少 6 位");
            userDao.changePassword(u.getId(), oldPassword, newPassword);
            return Result.ok("密码修改成功", null);
        });
    }

    public static Map<String, Object> userView(User u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("role", u.getRole());
        map.put("major", u.getMajor());
        map.put("phone", u.getPhone());
        map.put("loginTime", u.getLoginTime());
        map.put("createTime", u.getCreateTime());
        map.put("permissions", u.hasPermission());
        return map;
    }
}
