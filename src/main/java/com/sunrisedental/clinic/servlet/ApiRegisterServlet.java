package com.sunrisedental.clinic.servlet;

import com.google.gson.Gson;
import com.sunrisedental.clinic.model.StaffRegistrationForm;
import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * JSON API for public staff self-registration.
 */
public class ApiRegisterServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        StaffRegistrationForm form = gson.fromJson(body, StaffRegistrationForm.class);

        try {
            User user = ServiceFactory.getAuthService().registerStaff(form);
            JsonUtil.writeJson(resp, JsonUtil.success(ServiceFactory.getAuthService().toSessionUser(user)));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }
}
