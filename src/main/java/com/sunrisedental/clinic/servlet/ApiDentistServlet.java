package com.sunrisedental.clinic.servlet;

import com.sunrisedental.clinic.model.Dentist;
import com.sunrisedental.clinic.util.AuthUtil;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON API for dentist listing, availability, and admin dentist management.
 */
public class ApiDentistServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if ("true".equalsIgnoreCase(req.getParameter("available"))) {
                String dateParam = req.getParameter("date");
                String timeParam = req.getParameter("time");
                if (dateParam == null || timeParam == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, JsonUtil.error("Date and time are required."));
                    return;
                }
                List<Dentist> available = ServiceFactory.getDentistService()
                        .getAvailableDentists(LocalDate.parse(dateParam), LocalTime.parse(timeParam));
                JsonUtil.writeJson(resp, JsonUtil.success(available));
                return;
            }

            List<Dentist> dentists = ServiceFactory.getDentistService().getAllDentists();
            JsonUtil.writeJson(resp, JsonUtil.success(dentists));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            AuthUtil.requireAdmin(req.getSession(false));
            Dentist dentist = JsonUtil.gson().fromJson(readBody(req), Dentist.class);
            Dentist created = ServiceFactory.getDentistService().create(dentist);
            JsonUtil.writeJson(resp, JsonUtil.success(created));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            AuthUtil.requireAdmin(req.getSession(false));
            Dentist dentist = JsonUtil.gson().fromJson(readBody(req), Dentist.class);
            Dentist updated = ServiceFactory.getDentistService().update(dentist);
            JsonUtil.writeJson(resp, JsonUtil.success(updated));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            AuthUtil.requireAdmin(req.getSession(false));
            String idParam = req.getParameter("id");
            String activeParam = req.getParameter("active");
            if (idParam == null || activeParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, JsonUtil.error("Dentist id and active status are required."));
                return;
            }
            Dentist dentist = ServiceFactory.getDentistService()
                    .setActive(Integer.parseInt(idParam), Boolean.parseBoolean(activeParam));
            JsonUtil.writeJson(resp, JsonUtil.success(dentist));
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
