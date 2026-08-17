package com.sunrisedental.clinic.dao;

import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT id, username, password, full_name, mobile_number, role, active
                FROM users WHERE username = ?
                """;
        return queryOne(sql, username);
    }

    public Optional<User> findById(int id) {
        String sql = """
                SELECT id, username, password, full_name, mobile_number, role, active
                FROM users WHERE id = ?
                """;
        return queryOne(sql, id);
    }

    public List<User> findAllStaff() {
        String sql = """
                SELECT id, username, password, full_name, mobile_number, role, active
                FROM users ORDER BY role DESC, full_name ASC
                """;
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users.", e);
        }
        return users;
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByMobile(String mobileNumber) {
        String sql = "SELECT COUNT(*) FROM users WHERE mobile_number = ?";
        return count(sql, mobileNumber) > 0;
    }

    public void insert(User user) {
        String sql = """
                INSERT INTO users (username, password, full_name, mobile_number, role, active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getMobileNumber());
            stmt.setString(5, user.getRole());
            stmt.setBoolean(6, user.isActive());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user account.", e);
        }
    }

    public void update(User user) {
        String sql = """
                UPDATE users
                SET username = ?, full_name = ?, mobile_number = ?, role = ?, active = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getMobileNumber());
            stmt.setString(4, user.getRole());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user account.", e);
        }
    }

    public void updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset password.", e);
        }
    }

    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user account.", e);
        }
    }

    public long countActiveStaff() {
        return count("SELECT COUNT(*) FROM users WHERE role = 'STAFF' AND active = TRUE");
    }

    public long countAllUsers() {
        return count("SELECT COUNT(*) FROM users");
    }

    private Optional<User> queryOne(String sql, Object param) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while loading user.", e);
        }
        return Optional.empty();
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
            throw new RuntimeException("Database count failed.", e);
        }
        return 0;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setMobileNumber(rs.getString("mobile_number"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        return user;
    }
}
