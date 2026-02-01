-- =====================================================
-- SMART HIRING SYSTEM - INITIAL DATA
-- =====================================================

-- =====================================================
-- INSERT ROLES (REQUIRED)
-- Using ENUM names: ROLE_ADMIN, ROLE_HR, ROLE_CANDIDATE
-- =====================================================
INSERT INTO roles (id, name, description) VALUES
                                              (1, 'ROLE_ADMIN', 'System Administrator with full access'),
                                              (2, 'ROLE_HR', 'HR Manager who can post jobs and manage candidates'),
                                              (3, 'ROLE_CANDIDATE', 'Job seeker who can apply for jobs')
    ON DUPLICATE KEY UPDATE description = VALUES(description);

-- =====================================================
-- INSERT DEFAULT ADMIN USER
-- Password: Admin@123 (BCrypt encoded)
-- =====================================================
INSERT INTO users (id, email, password, full_name, phone, is_active, email_verified, created_at, updated_at) VALUES
    (1, 'admin@smarthiring.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqzuJLdPVVCGKBLPMNNODtQMsEXfC', 'System Admin', '9999999999', TRUE, TRUE, NOW(), NOW())
    ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1)
    ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- =====================================================
-- INSERT DEMO HR USER
-- Password: Hr@12345 (BCrypt encoded)
-- =====================================================
INSERT INTO users (id, email, password, full_name, phone, is_active, email_verified, created_at, updated_at) VALUES
    (2, 'hr@smarthiring.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'HR Manager', '8888888888', TRUE, TRUE, NOW(), NOW())
    ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Assign HR role to HR user
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2)
    ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- =====================================================
-- VERIFICATION
-- =====================================================
SELECT 'Initial data loaded successfully!' as Status;