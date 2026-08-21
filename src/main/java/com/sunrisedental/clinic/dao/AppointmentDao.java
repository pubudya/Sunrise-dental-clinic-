package com.sunrisedental.clinic.dao;

import com.sunrisedental.clinic.model.Appointment;
import com.sunrisedental.clinic.model.AppointmentSearchForm;
import com.sunrisedental.clinic.util.DatabaseConnection;
import com.sunrisedental.clinic.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access for patient appointments, including search, slot checks, and cancellation.
 */
public class AppointmentDao {

    public String generateNextAppointmentNumber() {
        String sql = "SELECT appointment_number FROM appointments ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String last = rs.getString("appointment_number");
                int number = Integer.parseInt(last.replace("APT", ""));
                return String.format("APT%04d", number + 1);
            }
            return "APT0001";
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate appointment number.", e);
        }
    }

    public void insert(Appointment appointment) {
        String sql = """
                INSERT INTO appointments
                (appointment_number, patient_name, address, contact_number, dentist_id, dentist_name,
                 treatment_id, treatment_type, appointment_date, appointment_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appointment.getAppointmentNumber());
            stmt.setString(2, appointment.getPatientName());
            stmt.setString(3, appointment.getAddress());
            stmt.setString(4, appointment.getContactNumber());
            stmt.setInt(5, appointment.getDentistId());
            stmt.setString(6, appointment.getDentistName());
            stmt.setInt(7, appointment.getTreatmentId());
            stmt.setString(8, appointment.getTreatmentType());
            stmt.setDate(9, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(10, Time.valueOf(appointment.getAppointmentTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalStateException("This dentist is already booked for the selected date and time.");
            }
            throw new RuntimeException("Failed to save appointment.", e);
        }
    }

    public Optional<Appointment> findByAppointmentNumber(String appointmentNumber) {
        String sql = baseSelect() + " WHERE a.appointment_number = ?";
        return queryOne(sql, appointmentNumber.trim().toUpperCase());
    }

    public Optional<Appointment> findById(int id) {
        String sql = baseSelect() + " WHERE a.id = ?";
        return queryOne(sql, id);
    }

    public List<Appointment> findAll() {
        String sql = baseSelect() + " ORDER BY a.appointment_date ASC, a.appointment_time ASC";
        return queryList(sql);
    }

    public List<Appointment> search(AppointmentSearchForm form) {
        StringBuilder sql = new StringBuilder(baseSelect() + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!ValidationUtil.isBlank(form.getAppointmentNumber())) {
            sql.append(" AND a.appointment_number = ?");
            params.add(form.getAppointmentNumber().trim().toUpperCase());
        }
        if (!ValidationUtil.isBlank(form.getPatientName())) {
            sql.append(" AND LOWER(a.patient_name) LIKE ?");
            params.add("%" + form.getPatientName().trim().toLowerCase() + "%");
        }
        if (!ValidationUtil.isBlank(form.getContactNumber())) {
            sql.append(" AND a.contact_number = ?");
            params.add(form.getContactNumber().trim());
        }
        sql.append(" ORDER BY a.appointment_date ASC, a.appointment_time ASC");

        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search appointments.", e);
        }
        return appointments;
    }

    public long countAll() {
        return count("SELECT COUNT(*) FROM appointments");
    }

    public long countToday() {
        return count("SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()");
    }

    public boolean isSlotTaken(int dentistId, LocalDate date, LocalTime time) {
        String sql = """
                SELECT COUNT(*) FROM appointments
                WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ?
                """;
        return count(sql, dentistId, java.sql.Date.valueOf(date), Time.valueOf(time)) > 0;
    }

    public boolean deleteByAppointmentNumber(String appointmentNumber) {
        String sql = "DELETE FROM appointments WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appointmentNumber.trim().toUpperCase());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cancel appointment.", e);
        }
    }

    private String baseSelect() {
        return """
                SELECT a.id, a.appointment_number, a.patient_name, a.address, a.contact_number,
                       a.dentist_id, a.dentist_name, a.treatment_id, a.treatment_type,
                       a.appointment_date, a.appointment_time
                FROM appointments a
                """;
    }

    private Optional<Appointment> queryOne(String sql, Object param) {
        List<Appointment> list = queryList(sql, param);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<Appointment> queryList(String sql, Object... params) {
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appointments.", e);
        }
        return appointments;
    }

    private long count(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count appointments.", e);
        }
        return 0;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("id"));
        appointment.setAppointmentNumber(rs.getString("appointment_number"));
        appointment.setPatientName(rs.getString("patient_name"));
        appointment.setAddress(rs.getString("address"));
        appointment.setContactNumber(rs.getString("contact_number"));
        appointment.setDentistId(rs.getInt("dentist_id"));
        appointment.setDentistName(rs.getString("dentist_name"));
        appointment.setTreatmentId(rs.getInt("treatment_id"));
        appointment.setTreatmentType(rs.getString("treatment_type"));
        appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        Time time = rs.getTime("appointment_time");
        appointment.setAppointmentTime(time != null ? time.toLocalTime() : LocalTime.MIDNIGHT);
        return appointment;
    }
}
