package com.libraryms.filter;

import com.libraryms.util.JsonUtil;
import com.libraryms.util.Result;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** 会话校验：除登录/验证码/注册外，所有 /api/* 请求必须已登录 */
@WebFilter("/api/*")
public class AuthFilter implements Filter {

    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
            "/api/captcha", "/api/auth/login", "/api/auth/current",
            "/api/auth/logout", "/api/register"));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        if (!WHITE_LIST.contains(uri) && req.getSession().getAttribute("user") == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(Result.fail("未登录或会话已过期，请重新登录")));
            return;
        }
        chain.doFilter(request, response);
    }
}
