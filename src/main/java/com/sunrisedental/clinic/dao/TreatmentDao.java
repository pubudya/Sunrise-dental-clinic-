package com.sunrisedental.clinic.dao;

import com.sunrisedental.clinic.model.Treatment;
import com.sunrisedental.clinic.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access for treatment types and costs used when booking and billing.
 */
public class TreatmentDao {

    public List<Treatment> findActive() {
        return findAll(true);
    }

    public List<Treatment> findAll() {
        return findAll(null);
    }

    public Optional<Treatment> findById(int id) {
        String sql = "SELECT id, name, cost, active FROM treatments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTreatment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load treatment.", e);
        }
        return Optional.empty();
    }

    public Optional<Treatment> findByName(String name) {
        String sql = "SELECT id, name, cost, active FROM treatments WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTreatment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load treatment.", e);
        }
        return Optional.empty();
    }

    public long countActive() {
        return count("SELECT COUNT(*) FROM treatments WHERE active = TRUE");
    }

    private List<Treatment> findAll(Boolean activeOnly) {
        String sql = "SELECT id, name, cost, active FROM treatments"
                + (activeOnly == null ? "" : " WHERE active = TRUE")
                + " ORDER BY name ASC";
        List<Treatment> treatments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                treatments.add(mapTreatment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load treatments.", e);
        }
        return treatments;
    }

    private long count(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count treatments.", e);
        }
        return 0;
    }

    private Treatment mapTreatment(ResultSet rs) throws SQLException {
        Treatment treatment = new Treatment();
        treatment.setId(rs.getInt("id"));
        treatment.setName(rs.getString("name"));
        treatment.setCost(rs.getDouble("cost"));
        treatment.setActive(rs.getBoolean("active"));
        return treatment;
    }
}
