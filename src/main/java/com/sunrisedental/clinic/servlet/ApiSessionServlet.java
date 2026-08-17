package com.sunrisedental.clinic.servlet;

import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import com.sunrisedental.clinic.util.SessionConstants;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class ApiSessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(SessionConstants.LOGGED_IN_USER) != null) {
            User user = (User) session.getAttribute(SessionConstants.LOGGED_IN_USER);
            JsonUtil.writeJson(resp, JsonUtil.success(ServiceFactory.getAuthService().toSessionUser(user)));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonUtil.writeJson(resp, JsonUtil.error("Not logged in."));
    }
}
