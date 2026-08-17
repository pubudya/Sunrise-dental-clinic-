package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.dao.AppointmentDao;
import com.sunrisedental.clinic.dao.DentistDao;
import com.sunrisedental.clinic.dao.TreatmentDao;
import com.sunrisedental.clinic.dao.UserDao;
import com.sunrisedental.clinic.util.RoleConstants;

import java.util.HashMap;
import java.util.Map;

public class DashboardService {

    private final AppointmentDao appointmentDao;
    private final DentistDao dentistDao;
    private final TreatmentDao treatmentDao;
    private final UserDao userDao;

    public DashboardService(AppointmentDao appointmentDao, DentistDao dentistDao,
                            TreatmentDao treatmentDao, UserDao userDao) {
        this.appointmentDao = appointmentDao;
        this.dentistDao = dentistDao;
        this.treatmentDao = treatmentDao;
        this.userDao = userDao;
    }

    public Map<String, Object> getStaffStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAppointments", appointmentDao.countAll());
        stats.put("todayAppointments", appointmentDao.countToday());
        stats.put("dentistCount", dentistDao.countActive());
        stats.put("treatmentCount", treatmentDao.countActive());
        return stats;
    }

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = getStaffStats();
        stats.put("staffCount", userDao.countActiveStaff());
        stats.put("totalUsers", userDao.countAllUsers());
        return stats;
    }

    public Map<String, Object> getStatsForRole(String role) {
        return RoleConstants.isAdmin(role) ? getAdminStats() : getStaffStats();
    }
}
