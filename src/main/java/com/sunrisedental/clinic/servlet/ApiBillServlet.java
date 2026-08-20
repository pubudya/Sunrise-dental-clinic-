package com.sunrisedental.clinic.servlet;

import com.google.gson.Gson;
import com.sunrisedental.clinic.model.BillForm;
import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.AuthUtil;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import com.sunrisedental.clinic.util.SessionConstants;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * JSON API for loading and saving appointment bills.
 */
public class ApiBillServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String number = req.getParameter("number");
        if (number == null || number.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error("Appointment number is required."));
            return;
        }

        try {
            JsonUtil.writeJson(resp, JsonUtil.success(
                    ServiceFactory.getBillService().getBillForAppointment(number)));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = AuthUtil.requireUser(req.getSession(false));
            String body = req.getReader().lines().collect(Collectors.joining());
            BillForm form = gson.fromJson(body, BillForm.class);
            JsonUtil.writeJson(resp, JsonUtil.success(
                    ServiceFactory.getBillService().calculateAndSave(form, user.getId())));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }
}
