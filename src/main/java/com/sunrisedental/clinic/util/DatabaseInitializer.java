package com.sunrisedental.clinic.util;

import com.sunrisedental.clinic.model.TreatmentType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates and migrates the database schema when the application starts.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        createDatabase();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        full_name VARCHAR(100) NOT NULL,
                        mobile_number VARCHAR(10) NOT NULL UNIQUE,
                        role VARCHAR(20) NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS dentists (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        specialization VARCHAR(100) NOT NULL,
                        mobile_number VARCHAR(10) NOT NULL,
                        work_start_time TIME NOT NULL,
                        work_end_time TIME NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS treatments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE,
                        cost DECIMAL(10,2) NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT TRUE
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        appointment_number VARCHAR(20) NOT NULL UNIQUE,
                        patient_name VARCHAR(100) NOT NULL,
                        address VARCHAR(255) NOT NULL,
                        contact_number VARCHAR(10) NOT NULL,
                        dentist_id INT NULL,
                        dentist_name VARCHAR(100) NOT NULL,
                        treatment_id INT NULL,
                        treatment_type VARCHAR(100) NOT NULL,
                        appointment_date DATE NOT NULL,
                        appointment_time TIME NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_dentist_slot (dentist_id, appointment_date, appointment_time)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bills (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        appointment_id INT NOT NULL UNIQUE,
                        treatment_cost DECIMAL(10,2) NOT NULL,
                        consultation_fee DECIMAL(10,2) NOT NULL,
                        discount_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
                        discount_value DECIMAL(10,2) NOT NULL DEFAULT 0,
                        discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                        total_amount DECIMAL(10,2) NOT NULL,
                        issued_by_user_id INT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_bills_appointment FOREIGN KEY (appointment_id)
                            REFERENCES appointments(id) ON DELETE CASCADE
                    )
                    """);

            migrateLegacyColumns(statement);
            seedDefaults(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Cannot connect to WAMP MySQL. Start WAMP and verify db.properties. Cause: "
                            + e.getMessage(), e);
        }
    }

    private static void migrateLegacyColumns(Statement statement) throws SQLException {
        tryExecute(statement, "ALTER TABLE users MODIFY password VARCHAR(255) NOT NULL");
        tryExecute(statement, "ALTER TABLE users ADD COLUMN mobile_number VARCHAR(10) NULL AFTER full_name");
        tryExecute(statement, "ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE AFTER role");
        tryExecute(statement, "UPDATE users SET mobile_number = CONCAT('07', LPAD(id, 8, '0')) WHERE mobile_number IS NULL");
        tryExecute(statement, "ALTER TABLE users MODIFY mobile_number VARCHAR(10) NOT NULL");
        tryExecute(statement, "ALTER TABLE users ADD UNIQUE KEY uk_users_mobile (mobile_number)");

        tryExecute(statement, "ALTER TABLE appointments ADD COLUMN dentist_id INT NULL AFTER contact_number");
        tryExecute(statement, "ALTER TABLE appointments ADD COLUMN treatment_id INT NULL AFTER dentist_name");
        tryExecute(statement, "ALTER TABLE appointments MODIFY contact_number VARCHAR(10) NOT NULL");
    }

    private static void seedDefaults(Statement statement) throws SQLException {
        String adminHash = PasswordUtil.hash("admin123");
        statement.executeUpdate("""
                INSERT INTO users (username, password, full_name, mobile_number, role, active)
                VALUES ('admin', '%s', 'Clinic Administrator', '0770000001', 'ADMIN', TRUE)
                ON DUPLICATE KEY UPDATE
                    password = VALUES(password),
                    full_name = VALUES(full_name),
                    mobile_number = VALUES(mobile_number),
                    role = 'ADMIN',
                    active = TRUE
                """.formatted(adminHash));

        seedDentist(statement, "Dr. Nimal Perera", "General Dentistry", "0771111111");
        seedDentist(statement, "Dr. Sanduni Fernando", "Orthodontics", "0772222222");
        seedDentist(statement, "Dr. Ruwan Silva", "Oral Surgery", "0773333333");
        seedDentist(statement, "Dr. Amaya Jayawardena", "Cosmetic Dentistry", "0774444444");

        for (String treatmentName : TreatmentType.getAllTypes()) {
            statement.executeUpdate("""
                    INSERT INTO treatments (name, cost, active)
                    VALUES ('%s', %.2f, TRUE)
                    ON DUPLICATE KEY UPDATE cost = VALUES(cost), active = TRUE
                    """.formatted(treatmentName.replace("'", "''"),
                    TreatmentType.getTreatmentCost(treatmentName)));
        }
    }

    private static void seedDentist(Statement statement, String name, String specialization, String mobile)
            throws SQLException {
        statement.executeUpdate("""
                INSERT INTO dentists (name, specialization, mobile_number, work_start_time, work_end_time, active)
                SELECT '%s', '%s', '%s', '08:00:00', '18:00:00', TRUE
                WHERE NOT EXISTS (SELECT 1 FROM dentists WHERE name = '%s')
                """.formatted(name.replace("'", "''"), specialization.replace("'", "''"),
                mobile, name.replace("'", "''")));
    }

    private static void tryExecute(Statement statement, String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException ignored) {
            // Column or constraint already exists.
        }
    }

    private static void createDatabase() {
        String databaseName = DatabaseConnection.getDatabaseName();
        try (Connection connection = DatabaseConnection.getServerConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS `" + databaseName
                            + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Cannot connect to WAMP MySQL. Start WAMP and verify db.properties. Cause: "
                            + e.getMessage(), e);
        }
    }
}
