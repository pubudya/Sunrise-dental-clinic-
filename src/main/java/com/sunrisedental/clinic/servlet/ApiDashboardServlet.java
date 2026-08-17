package com.sunrisedental.clinic.servlet;

import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import com.sunrisedental.clinic.util.SessionConstants;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ApiDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession(false).getAttribute(SessionConstants.LOGGED_IN_USER);
        JsonUtil.writeJson(resp, JsonUtil.success(
                ServiceFactory.getDashboardService().getStatsForRole(user.getRole())));
    }
}
