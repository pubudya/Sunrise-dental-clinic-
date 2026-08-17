package com.sunrisedental.clinic.dao;

import com.sunrisedental.clinic.model.Dentist;
import com.sunrisedental.clinic.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DentistDao {

    public List<Dentist> findAll() {
        return findByActiveStatus(null);
    }

    public List<Dentist> findActive() {
        return findByActiveStatus(true);
    }

    public Optional<Dentist> findById(int id) {
        String sql = """
                SELECT id, name, specialization, mobile_number, work_start_time, work_end_time, active
                FROM dentists WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDentist(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load dentist.", e);
        }
        return Optional.empty();
    }

    public List<Dentist> findAvailable(LocalDate date, LocalTime time) {
        String sql = """
                SELECT d.id, d.name, d.specialization, d.mobile_number, d.work_start_time,
                       d.work_end_time, d.active
                FROM dentists d
                WHERE d.active = TRUE
                  AND ? BETWEEN d.work_start_time AND d.work_end_time
                  AND NOT EXISTS (
                      SELECT 1 FROM appointments a
                      WHERE a.dentist_id = d.id
                        AND a.appointment_date = ?
                        AND a.appointment_time = ?
                  )
                ORDER BY d.name ASC
                """;
        List<Dentist> dentists = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTime(1, Time.valueOf(time));
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(time));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dentists.add(mapDentist(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load available dentists.", e);
        }
        return dentists;
    }

    public void insert(Dentist dentist) {
        String sql = """
                INSERT INTO dentists
                (name, specialization, mobile_number, work_start_time, work_end_time, active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindDentist(stmt, dentist);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    dentist.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create dentist.", e);
        }
    }

    public void update(Dentist dentist) {
        String sql = """
                UPDATE dentists
                SET name = ?, specialization = ?, mobile_number = ?,
                    work_start_time = ?, work_end_time = ?, active = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindDentist(stmt, dentist);
            stmt.setInt(7, dentist.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update dentist.", e);
        }
    }

    public long countActive() {
        return count("SELECT COUNT(*) FROM dentists WHERE active = TRUE");
    }

    private List<Dentist> findByActiveStatus(Boolean activeOnly) {
        String sql = """
                SELECT id, name, specialization, mobile_number, work_start_time, work_end_time, active
                FROM dentists
                """ + (activeOnly == null ? "" : " WHERE active = " + (activeOnly ? "TRUE" : "FALSE"))
                + " ORDER BY name ASC";
        List<Dentist> dentists = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dentists.add(mapDentist(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load dentists.", e);
        }
        return dentists;
    }

    private void bindDentist(PreparedStatement stmt, Dentist dentist) throws SQLException {
        stmt.setString(1, dentist.getName());
        stmt.setString(2, dentist.getSpecialization());
        stmt.setString(3, dentist.getMobileNumber());
        stmt.setTime(4, Time.valueOf(dentist.getWorkStartTime()));
        stmt.setTime(5, Time.valueOf(dentist.getWorkEndTime()));
        stmt.setBoolean(6, dentist.isActive());
    }

    private long count(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count dentists.", e);
        }
        return 0;
    }

    private Dentist mapDentist(ResultSet rs) throws SQLException {
        Dentist dentist = new Dentist();
        dentist.setId(rs.getInt("id"));
        dentist.setName(rs.getString("name"));
        dentist.setSpecialization(rs.getString("specialization"));
        dentist.setMobileNumber(rs.getString("mobile_number"));
        dentist.setWorkStartTime(rs.getTime("work_start_time").toLocalTime());
        dentist.setWorkEndTime(rs.getTime("work_end_time").toLocalTime());
        dentist.setActive(rs.getBoolean("active"));
        return dentist;
    }
}
