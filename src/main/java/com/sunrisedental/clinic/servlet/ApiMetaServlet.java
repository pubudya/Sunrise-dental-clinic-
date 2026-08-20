package com.sunrisedental.clinic.servlet;

import com.sunrisedental.clinic.model.TreatmentType;
import com.sunrisedental.clinic.util.JsonUtil;
import com.sunrisedental.clinic.util.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * JSON API for lookup data such as treatment types used by the HTML forms.
 */
public class ApiMetaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, JsonUtil.success(Map.of(
                "dentists", ServiceFactory.getDentistService().getActiveDentists(),
                "treatmentTypes", ServiceFactory.getTreatmentDao().findActive(),
                "consultationFee", TreatmentType.CONSULTATION_FEE
        )));
    }
}
