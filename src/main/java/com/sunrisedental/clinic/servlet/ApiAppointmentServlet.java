package com.sunrisedental.clinic.servlet;

import com.google.gson.Gson;
import com.sunrisedental.clinic.model.AppointmentForm;
import com.sunrisedental.clinic.model.AppointmentSearchForm;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import com.sunrisedental.clinic.util.ValidationUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON API for registering, listing, searching, and cancelling patient appointments.
 */
public class ApiAppointmentServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String number = req.getParameter("number");
        String patientName = req.getParameter("patientName");
        String contactNumber = req.getParameter("contactNumber");

        if (ValidationUtil.hasAnySearchCriteria(number, patientName, contactNumber)) {
            try {
                AppointmentSearchForm form = new AppointmentSearchForm();
                form.setAppointmentNumber(number);
                form.setPatientName(patientName);
                form.setContactNumber(contactNumber);
                JsonUtil.writeJson(resp, JsonUtil.success(ServiceFactory.getAppointmentService().search(form)));
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
            }
            return;
        }

        JsonUtil.writeJson(resp, JsonUtil.success(ServiceFactory.getAppointmentService().getAllAppointments()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        AppointmentForm form = gson.fromJson(body, AppointmentForm.class);

        try {
            String appointmentNumber = ServiceFactory.getAppointmentService().registerAppointment(form);
            JsonUtil.writeJson(resp, JsonUtil.success(Map.of(
                    "appointmentNumber", appointmentNumber,
                    "message", "Appointment registered successfully!"
            )));
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ServiceFactory.getAppointmentService().cancelAppointment(req.getParameter("number"));
            JsonUtil.writeJson(resp, JsonUtil.success(Map.of(
                    "message", "Appointment cancelled successfully. The dentist slot is now available."
            )));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }
}
