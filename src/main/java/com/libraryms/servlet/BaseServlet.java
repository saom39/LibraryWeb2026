package com.libraryms.servlet;

import com.libraryms.entity.User;
import com.libraryms.util.BizException;
import com.libraryms.util.JsonUtil;
import com.libraryms.util.Result;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Servlet 公共基类：统一 JSON 响应、参数读取、登录用户与角色校验 */
public abstract class BaseServlet extends HttpServlet {

    protected void write(HttpServletResponse resp, Object obj) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(JsonUtil.toJson(obj));
    }

    protected void ok(HttpServletResponse resp, Object data) throws IOException {
        write(resp, Result.ok(data));
    }

    protected void ok(HttpServletResponse resp, String message, Object data) throws IOException {
        write(resp, Result.ok(message, data));
    }

    protected void fail(HttpServletResponse resp, String message) throws IOException {
        write(resp, Result.fail(message));
    }

    /** 统一异常处理：业务异常直接把提示回显到页面 */
    protected void run(HttpServletResponse resp, Supplier<Object> action) throws IOException {
        try {
            write(resp, action.get());
        } catch (BizException e) {
            write(resp, Result.fail(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            write(resp, Result.fail("服务器异常：" + e.getMessage()));
        }
    }

    /** 读取并缓存 POST 的 JSON 请求体 */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> bodyMap(HttpServletRequest req) {
        Object cached = req.getAttribute("_jsonBody");
        if (cached instanceof Map) return (Map<String, Object>) cached;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        } catch (IOException e) {
            throw new RuntimeException("读取请求体失败", e);
        }
        Map<String, Object> map = sb.length() == 0
                ? new LinkedHashMap<>() : JsonUtil.fromJsonMap(sb.toString());
        req.setAttribute("_jsonBody", map);
        return map;
    }

    /** 同时支持 query 参数与 JSON 请求体 */
    protected String param(HttpServletRequest req, String key) {
        String v = req.getParameter(key);
        if (v != null) return v;
        Object o = bodyMap(req).get(key);
        return o == null ? null : String.valueOf(o);
    }

    protected int intParam(HttpServletRequest req, String key, int defaultValue) {
        String v = param(req, key);
        if (v == null || v.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new BizException(key + " 必须是整数");
        }
    }

    protected double doubleParam(HttpServletRequest req, String key, double defaultValue) {
        String v = param(req, key);
        if (v == null || v.trim().isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            throw new BizException(key + " 必须是数字");
        }
    }

    protected User currentUser(HttpServletRequest req) {
        Object u = req.getSession().getAttribute("user");
        return u instanceof User ? (User) u : null;
    }

    protected String currentUserId(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) throw new BizException("请先登录");
        return u.getId();
    }

    protected void requireRole(HttpServletRequest req, String... roles) {
        User u = currentUser(req);
        if (u == null) throw new BizException("请先登录");
        for (String r : roles) {
            if (r.equals(u.getRole())) return;
        }
        throw new BizException("当前角色无权执行该操作");
    }
}
