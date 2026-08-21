package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.dao.AppointmentDao;
import com.sunrisedental.clinic.dao.DentistDao;
import com.sunrisedental.clinic.dao.TreatmentDao;
import com.sunrisedental.clinic.model.Appointment;
import com.sunrisedental.clinic.model.AppointmentForm;
import com.sunrisedental.clinic.model.AppointmentSearchForm;
import com.sunrisedental.clinic.model.Dentist;
import com.sunrisedental.clinic.model.Treatment;
import com.sunrisedental.clinic.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class AppointmentService {

    private static final LocalTime CLINIC_OPEN = LocalTime.of(8, 0);
    private static final LocalTime CLINIC_CLOSE = LocalTime.of(18, 0);

    private final AppointmentDao appointmentDao;
    private final DentistDao dentistDao;
    private final TreatmentDao treatmentDao;

    public AppointmentService(AppointmentDao appointmentDao, DentistDao dentistDao, TreatmentDao treatmentDao) {
        this.appointmentDao = appointmentDao;
        this.dentistDao = dentistDao;
        this.treatmentDao = treatmentDao;
    }

    public String registerAppointment(AppointmentForm form) {
        validateForm(form);

        int dentistId = Integer.parseInt(form.getDentistId());
        int treatmentId = Integer.parseInt(form.getTreatmentId());
        LocalDate date = LocalDate.parse(form.getAppointmentDate());
        LocalTime time = LocalTime.parse(form.getAppointmentTime());

        Dentist dentist = dentistDao.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("Selected dentist was not found."));
        Treatment treatment = treatmentDao.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException("Selected treatment was not found."));

        if (!dentist.isActive()) {
            throw new IllegalArgumentException("Selected dentist is inactive.");
        }
        if (!treatment.isActive()) {
            throw new IllegalArgumentException("Selected treatment is inactive.");
        }
        if (time.isBefore(dentist.getWorkStartTime()) || time.isAfter(dentist.getWorkEndTime())) {
            throw new IllegalArgumentException("Selected time is outside dentist working hours.");
        }
        if (appointmentDao.isSlotTaken(dentistId, date, time)) {
            throw new IllegalStateException("This dentist is already booked for the selected date and time.");
        }

        String appointmentNumber = appointmentDao.generateNextAppointmentNumber();
        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber(appointmentNumber);
        appointment.setPatientName(form.getPatientName().trim());
        appointment.setAddress(form.getAddress().trim());
        appointment.setContactNumber(form.getContactNumber().trim());
        appointment.setDentistId(dentist.getId());
        appointment.setDentistName(dentist.getName());
        appointment.setTreatmentId(treatment.getId());
        appointment.setTreatmentType(treatment.getName());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);

        appointmentDao.insert(appointment);
        return appointmentNumber;
    }

    public Optional<Appointment> findByAppointmentNumber(String appointmentNumber) {
        if (ValidationUtil.isBlank(appointmentNumber)) {
            return Optional.empty();
        }
        return appointmentDao.findByAppointmentNumber(appointmentNumber);
    }

    public List<Appointment> search(AppointmentSearchForm form) {
        if (!ValidationUtil.hasAnySearchCriteria(
                form.getAppointmentNumber(), form.getPatientName(), form.getContactNumber())) {
            throw new IllegalArgumentException("Enter at least one search field.");
        }
        if (!ValidationUtil.isBlank(form.getContactNumber())
                && !form.getContactNumber().trim().matches(ValidationUtil.MOBILE_PATTERN)) {
            throw new IllegalArgumentException("Mobile number must be exactly 10 digits.");
        }
        return appointmentDao.search(form);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDao.findAll();
    }

    public void cancelAppointment(String appointmentNumber) {
        if (ValidationUtil.isBlank(appointmentNumber)) {
            throw new IllegalArgumentException("Appointment number is required.");
        }
        String number = appointmentNumber.trim().toUpperCase();
        appointmentDao.findByAppointmentNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Appointment was not found."));
        if (!appointmentDao.deleteByAppointmentNumber(number)) {
            throw new IllegalArgumentException("Appointment was not found.");
        }
    }

    public long getTotalAppointments() {
        return appointmentDao.countAll();
    }

    public long getTodayAppointments() {
        return appointmentDao.countToday();
    }

    private void validateForm(AppointmentForm form) {
        ValidationUtil.requireNonBlank(form.getPatientName(), "Patient name");
        ValidationUtil.requireNonBlank(form.getAddress(), "Address");
        ValidationUtil.requireMobile(form.getContactNumber(), "Contact number");
        ValidationUtil.requireNonBlank(form.getDentistId(), "Dentist");
        ValidationUtil.requireNonBlank(form.getTreatmentId(), "Treatment type");
        ValidationUtil.requireNonBlank(form.getAppointmentDate(), "Appointment date");
        ValidationUtil.requireNonBlank(form.getAppointmentTime(), "Appointment time");

        try {
            LocalDate date = LocalDate.parse(form.getAppointmentDate());
            LocalTime time = LocalTime.parse(form.getAppointmentTime());
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Appointment date cannot be in the past.");
            }
            if (time.isBefore(CLINIC_OPEN) || time.isAfter(CLINIC_CLOSE)) {
                throw new IllegalArgumentException("Clinic hours are 08:00 to 18:00.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date or time format.");
        }
    }
}
