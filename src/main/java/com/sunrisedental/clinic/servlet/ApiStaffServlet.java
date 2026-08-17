package com.sunrisedental.clinic.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.AuthUtil;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiStaffServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            AuthUtil.requireAdmin(req.getSession(false));
            List<Map<String, Object>> staff = ServiceFactory.getUserService().getAllUsers().stream()
                    .map(ServiceFactory.getUserService()::toPublicView)
                    .toList();
            JsonUtil.writeJson(resp, JsonUtil.success(staff));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User admin = AuthUtil.requireAdmin(req.getSession(false));
            User incoming = gson.fromJson(readBody(req), User.class);
            User updated = ServiceFactory.getUserService().updateStaff(incoming, admin.getId());
            JsonUtil.writeJson(resp, JsonUtil.success(ServiceFactory.getUserService().toPublicView(updated)));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User admin = AuthUtil.requireAdmin(req.getSession(false));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> payload = gson.fromJson(readBody(req), type);
            String action = String.valueOf(payload.getOrDefault("action", ""));

            switch (action) {
                case "resetPassword" -> {
                    int userId = ((Number) payload.get("userId")).intValue();
                    String newPassword = String.valueOf(payload.get("newPassword"));
                    ServiceFactory.getUserService().resetPassword(userId, newPassword, admin.getId());
                    JsonUtil.writeJson(resp, JsonUtil.success(Map.of("message", "Password reset successfully.")));
                }
                case "delete" -> {
                    int userId = ((Number) payload.get("userId")).intValue();
                    ServiceFactory.getUserService().deleteStaff(userId, admin.getId());
                    JsonUtil.writeJson(resp, JsonUtil.success(Map.of("message", "Staff account deleted.")));
                }
                default -> {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, JsonUtil.error("Unsupported staff action."));
                }
            }
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        return req.getReader().lines().collect(Collectors.joining());
    }
}
