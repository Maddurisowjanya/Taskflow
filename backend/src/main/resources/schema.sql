-- ==========================================
-- TaskFlow Database Schema
-- ==========================================
-- Run this script to create the database and tables manually
-- (Hibernate will auto-create tables if spring.jpa.hibernate.ddl-auto=update)

-- Create the database
CREATE DATABASE IF NOT EXISTS taskflow_db;
USE taskflow_db;

-- ==========================================
-- Table: roles
-- Stores user roles (ROLE_USER, ROLE_ADMIN)
-- ==========================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- Table: users
-- Stores registered user accounts
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,         -- BCrypt hashed password
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    avatar_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- Table: user_roles (Many-to-Many join table)
-- Links users to their roles
-- ==========================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ==========================================
-- Table: tasks
-- Stores all tasks created by users
-- ==========================================
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'TODO',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    category VARCHAR(100),
    due_date DATE,
    completed_at TIMESTAMP NULL,
    user_id BIGINT NOT NULL,                -- Owner of the task
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================
-- Seed Data: Default Roles
-- ==========================================
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');

-- ==========================================
-- Sample Data (Optional - for testing)
-- ==========================================
-- Note: Password is 'password123' hashed with BCrypt
INSERT IGNORE INTO users (username, email, password, first_name, last_name, is_active)
VALUES ('admin', 'admin@taskflow.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKDRVFHTc9DPUI5bR.7L5DVLC5iy',
        'Admin', 'User', TRUE);

-- Assign ADMIN role to the admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
