package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.dao.DentistDao;
import com.sunrisedental.clinic.model.Dentist;
import com.sunrisedental.clinic.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DentistService {

    private final DentistDao dentistDao;

    public DentistService(DentistDao dentistDao) {
        this.dentistDao = dentistDao;
    }

    public List<Dentist> getAllDentists() {
        return dentistDao.findAll();
    }

    public List<Dentist> getActiveDentists() {
        return dentistDao.findActive();
    }

    public List<Dentist> getAvailableDentists(LocalDate date, LocalTime time) {
        validateDateTime(date, time);
        return dentistDao.findAvailable(date, time);
    }

    public Dentist getById(int id) {
        return dentistDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dentist not found."));
    }

    public Dentist create(Dentist dentist) {
        validateDentist(dentist);
        dentist.setActive(true);
        dentistDao.insert(dentist);
        return dentist;
    }

    public Dentist update(Dentist dentist) {
        validateDentist(dentist);
        dentistDao.findById(dentist.getId())
                .orElseThrow(() -> new IllegalArgumentException("Dentist not found."));
        dentistDao.update(dentist);
        return dentist;
    }

    public Dentist setActive(int id, boolean active) {
        Dentist dentist = getById(id);
        dentist.setActive(active);
        dentistDao.update(dentist);
        return dentist;
    }

    public long countActive() {
        return dentistDao.countActive();
    }

    private void validateDentist(Dentist dentist) {
        ValidationUtil.requireNonBlank(dentist.getName(), "Dentist name");
        ValidationUtil.requireNonBlank(dentist.getSpecialization(), "Specialization");
        ValidationUtil.requireMobile(dentist.getMobileNumber(), "Dentist mobile number");
        if (dentist.getWorkStartTime() == null || dentist.getWorkEndTime() == null) {
            throw new IllegalArgumentException("Working hours are required.");
        }
        if (!dentist.getWorkStartTime().isBefore(dentist.getWorkEndTime())) {
            throw new IllegalArgumentException("Work start time must be before end time.");
        }
    }

    private void validateDateTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            throw new IllegalArgumentException("Date and time are required to check availability.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Availability date cannot be in the past.");
        }
    }
}
