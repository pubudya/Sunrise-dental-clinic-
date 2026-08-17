package com.sunrisedental.clinic.servlet;

import com.google.gson.Gson;
import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import com.sunrisedental.clinic.util.SessionConstants;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApiLoginServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        LoginRequest loginRequest = gson.fromJson(body, LoginRequest.class);

        Optional<User> user = ServiceFactory.getAuthService().authenticate(
                loginRequest.username, loginRequest.password);

        if (user.isPresent()) {
            HttpSession session = req.getSession(true);
            session.setAttribute(SessionConstants.LOGGED_IN_USER, user.get());
            JsonUtil.writeJson(resp, JsonUtil.success(
                    ServiceFactory.getAuthService().toSessionUser(user.get())));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonUtil.writeJson(resp, JsonUtil.error("Invalid username or password, or account is inactive."));
    }

    private static class LoginRequest {
        String username;
        String password;
    }
}
