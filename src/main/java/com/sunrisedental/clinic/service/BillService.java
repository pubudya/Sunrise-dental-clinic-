package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.dao.AppointmentDao;
import com.sunrisedental.clinic.dao.BillDao;
import com.sunrisedental.clinic.dao.TreatmentDao;
import com.sunrisedental.clinic.model.Appointment;
import com.sunrisedental.clinic.model.Bill;
import com.sunrisedental.clinic.model.BillForm;
import com.sunrisedental.clinic.model.Treatment;
import com.sunrisedental.clinic.model.TreatmentType;
import com.sunrisedental.clinic.util.ValidationUtil;

public class BillService {

    public static final String DISCOUNT_NONE = "NONE";
    public static final String DISCOUNT_PERCENT = "PERCENT";
    public static final String DISCOUNT_FIXED = "FIXED";

    private final AppointmentDao appointmentDao;
    private final TreatmentDao treatmentDao;
    private final BillDao billDao;

    public BillService(AppointmentDao appointmentDao, TreatmentDao treatmentDao, BillDao billDao) {
        this.appointmentDao = appointmentDao;
        this.treatmentDao = treatmentDao;
        this.billDao = billDao;
    }

    public Bill getBillForAppointment(String appointmentNumber) {
        Appointment appointment = appointmentDao.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        Bill bill = billDao.findByAppointmentId(appointment.getId()).orElseGet(() -> createDefaultBill(appointment));
        bill.setAppointment(appointment);
        return bill;
    }

    public Bill calculateAndSave(BillForm form, int issuedByUserId) {
        if (ValidationUtil.isBlank(form.getAppointmentNumber())) {
            throw new IllegalArgumentException("Appointment number is required.");
        }

        Appointment appointment = appointmentDao.findByAppointmentNumber(form.getAppointmentNumber())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        Treatment treatment = treatmentDao.findById(appointment.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("Treatment details not found."));

        double treatmentCost = treatment.getCost();
        double consultationFee = form.getConsultationFee() >= 0
                ? form.getConsultationFee()
                : TreatmentType.CONSULTATION_FEE;
        if (consultationFee < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative.");
        }

        String discountType = ValidationUtil.isBlank(form.getDiscountType())
                ? DISCOUNT_NONE : form.getDiscountType().trim().toUpperCase();
        double discountValue = Math.max(0, form.getDiscountValue());
        double subtotal = treatmentCost + consultationFee;
        double discountAmount = calculateDiscountAmount(subtotal, discountType, discountValue);
        double totalAmount = Math.max(0, subtotal - discountAmount);

        Bill bill = new Bill();
        bill.setAppointment(appointment);
        bill.setTreatmentCost(treatmentCost);
        bill.setConsultationFee(consultationFee);
        bill.setDiscountType(discountType);
        bill.setDiscountValue(discountValue);
        bill.setDiscountAmount(discountAmount);
        bill.setTotalAmount(totalAmount);

        billDao.save(bill, appointment.getId(), issuedByUserId);
        return bill;
    }

    private Bill createDefaultBill(Appointment appointment) {
        Treatment treatment = treatmentDao.findById(appointment.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("Treatment details not found."));
        Bill bill = new Bill();
        bill.setTreatmentCost(treatment.getCost());
        bill.setConsultationFee(TreatmentType.CONSULTATION_FEE);
        bill.setDiscountType(DISCOUNT_NONE);
        bill.setDiscountValue(0);
        bill.setDiscountAmount(0);
        bill.setTotalAmount(treatment.getCost() + TreatmentType.CONSULTATION_FEE);
        return bill;
    }

    private double calculateDiscountAmount(double subtotal, String discountType, double discountValue) {
        return switch (discountType) {
            case DISCOUNT_PERCENT -> {
                if (discountValue > 100) {
                    throw new IllegalArgumentException("Percentage discount cannot exceed 100.");
                }
                yield subtotal * discountValue / 100.0;
            }
            case DISCOUNT_FIXED -> {
                if (discountValue > subtotal) {
                    throw new IllegalArgumentException("Fixed discount cannot exceed subtotal.");
                }
                yield discountValue;
            }
            case DISCOUNT_NONE -> 0;
            default -> throw new IllegalArgumentException("Invalid discount type.");
        };
    }
}
