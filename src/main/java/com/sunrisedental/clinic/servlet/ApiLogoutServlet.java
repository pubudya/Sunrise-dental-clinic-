package com.sunrisedental.clinic.servlet;

import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.SessionConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * JSON API that ends the current staff or admin session.
 */
@WebServlet("/api/logout")
public class ApiLogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.writeJson(resp, JsonUtil.success(Map.of("message", "Logged out successfully.")));
    }
}
