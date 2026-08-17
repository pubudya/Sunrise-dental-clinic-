package com.sunrisedental.clinic.util;

import com.sunrisedental.clinic.dao.AppointmentDao;
import com.sunrisedental.clinic.dao.BillDao;
import com.sunrisedental.clinic.dao.DentistDao;
import com.sunrisedental.clinic.dao.TreatmentDao;
import com.sunrisedental.clinic.dao.UserDao;
import com.sunrisedental.clinic.service.AppointmentService;
import com.sunrisedental.clinic.service.AuthService;
import com.sunrisedental.clinic.service.BillService;
import com.sunrisedental.clinic.service.DashboardService;
import com.sunrisedental.clinic.service.DentistService;
import com.sunrisedental.clinic.service.UserService;

/**
 * Factory that provides service instances without a dependency injection framework.
 */
public final class ServiceFactory {

    private static final UserDao USER_DAO = new UserDao();
    private static final DentistDao DENTIST_DAO = new DentistDao();
    private static final TreatmentDao TREATMENT_DAO = new TreatmentDao();
    private static final AppointmentDao APPOINTMENT_DAO = new AppointmentDao();
    private static final BillDao BILL_DAO = new BillDao();

    private static final AuthService AUTH_SERVICE = new AuthService(USER_DAO);
    private static final UserService USER_SERVICE = new UserService(USER_DAO);
    private static final DentistService DENTIST_SERVICE = new DentistService(DENTIST_DAO);
    private static final AppointmentService APPOINTMENT_SERVICE =
            new AppointmentService(APPOINTMENT_DAO, DENTIST_DAO, TREATMENT_DAO);
    private static final BillService BILL_SERVICE =
            new BillService(APPOINTMENT_DAO, TREATMENT_DAO, BILL_DAO);
    private static final DashboardService DASHBOARD_SERVICE =
            new DashboardService(APPOINTMENT_DAO, DENTIST_DAO, TREATMENT_DAO, USER_DAO);

    private ServiceFactory() {
    }

    public static AuthService getAuthService() {
        return AUTH_SERVICE;
    }

    public static UserService getUserService() {
        return USER_SERVICE;
    }

    public static DentistService getDentistService() {
        return DENTIST_SERVICE;
    }

    public static AppointmentService getAppointmentService() {
        return APPOINTMENT_SERVICE;
    }

    public static BillService getBillService() {
        return BILL_SERVICE;
    }

    public static DashboardService getDashboardService() {
        return DASHBOARD_SERVICE;
    }

    public static TreatmentDao getTreatmentDao() {
        return TREATMENT_DAO;
    }
}
