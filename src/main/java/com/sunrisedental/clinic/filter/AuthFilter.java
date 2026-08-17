package com.sunrisedental.clinic.filter;

import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.RoleConstants;
import com.sunrisedental.clinic.util.SessionConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Protects API endpoints — only logged-in staff can access them (except login/register/session).
 */
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of("/api/login", "/api/session", "/api/register");
    private static final Set<String> ADMIN_PATHS = Set.of("/api/staff");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin") != null ? req.getHeader("Origin") : "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = req.getServletPath();

        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session != null
                ? (User) session.getAttribute(SessionConstants.LOGGED_IN_USER) : null;

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, JsonUtil.error("Please log in to continue."));
            return;
        }

        if (requiresAdmin(path, req.getMethod()) && !RoleConstants.isAdmin(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, JsonUtil.error("Admin access required."));
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean requiresAdmin(String path, String method) {
        if (ADMIN_PATHS.contains(path)) {
            return true;
        }
        if ("/api/dentists".equals(path) && !"GET".equalsIgnoreCase(method)) {
            return true;
        }
        return false;
    }
}
