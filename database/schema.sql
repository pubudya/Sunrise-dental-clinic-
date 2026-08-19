-- Sunrise Dental Clinic - MySQL Database Schema
-- Version 1.0
-- The Java application also creates/migrates this schema automatically on startup.
-- Run this script in phpMyAdmin or MySQL CLI for manual setup.

CREATE DATABASE IF NOT EXISTS sunrise_dental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sunrise_dental;

CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(10)  NOT NULL UNIQUE,
    role            VARCHAR(20)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dentists (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    specialization  VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(10)  NOT NULL,
    work_start_time TIME         NOT NULL,
    work_end_time   TIME         NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS treatments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    cost            DECIMAL(10,2) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS appointments (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number  VARCHAR(20)  NOT NULL UNIQUE,
    patient_name        VARCHAR(100) NOT NULL,
    address             VARCHAR(255) NOT NULL,
    contact_number      VARCHAR(10)  NOT NULL,
    dentist_id          INT NULL,
    dentist_name        VARCHAR(100) NOT NULL,
    treatment_id        INT NULL,
    treatment_type      VARCHAR(100) NOT NULL,
    appointment_date    DATE         NOT NULL,
    appointment_time    TIME         NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dentist_slot (dentist_id, appointment_date, appointment_time)
);

CREATE TABLE IF NOT EXISTS bills (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id      INT NOT NULL UNIQUE,
    treatment_cost      DECIMAL(10,2) NOT NULL,
    consultation_fee    DECIMAL(10,2) NOT NULL,
    discount_type       VARCHAR(20) NOT NULL DEFAULT 'NONE',
    discount_value      DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(10,2) NOT NULL,
    issued_by_user_id   INT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointments(id) ON DELETE CASCADE
);

-- Default admin account (password hashed by application on startup if using auto-init)
-- Login: admin / admin123
