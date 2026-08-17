package com.sunrisedental.clinic.dao;

import com.sunrisedental.clinic.model.Bill;
import com.sunrisedental.clinic.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class BillDao {

    public Optional<Bill> findByAppointmentId(int appointmentId) {
        String sql = """
                SELECT b.id, b.appointment_id, b.treatment_cost, b.consultation_fee, b.discount_type,
                       b.discount_value, b.discount_amount, b.total_amount, u.full_name AS issued_by_name
                FROM bills b
                LEFT JOIN users u ON u.id = b.issued_by_user_id
                WHERE b.appointment_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Bill bill = new Bill();
                    bill.setId(rs.getInt("id"));
                    bill.setTreatmentCost(rs.getDouble("treatment_cost"));
                    bill.setConsultationFee(rs.getDouble("consultation_fee"));
                    bill.setDiscountType(rs.getString("discount_type"));
                    bill.setDiscountValue(rs.getDouble("discount_value"));
                    bill.setDiscountAmount(rs.getDouble("discount_amount"));
                    bill.setTotalAmount(rs.getDouble("total_amount"));
                    bill.setIssuedByName(rs.getString("issued_by_name"));
                    return Optional.of(bill);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load bill.", e);
        }
        return Optional.empty();
    }

    public void save(Bill bill, int appointmentId, int issuedByUserId) {
        String sql = """
                INSERT INTO bills
                (appointment_id, treatment_cost, consultation_fee, discount_type, discount_value,
                 discount_amount, total_amount, issued_by_user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    treatment_cost = VALUES(treatment_cost),
                    consultation_fee = VALUES(consultation_fee),
                    discount_type = VALUES(discount_type),
                    discount_value = VALUES(discount_value),
                    discount_amount = VALUES(discount_amount),
                    total_amount = VALUES(total_amount),
                    issued_by_user_id = VALUES(issued_by_user_id)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, appointmentId);
            stmt.setDouble(2, bill.getTreatmentCost());
            stmt.setDouble(3, bill.getConsultationFee());
            stmt.setString(4, bill.getDiscountType());
            stmt.setDouble(5, bill.getDiscountValue());
            stmt.setDouble(6, bill.getDiscountAmount());
            stmt.setDouble(7, bill.getTotalAmount());
            stmt.setInt(8, issuedByUserId);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bill.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save bill.", e);
        }
    }
}
