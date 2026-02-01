-- =====================================================
-- SMART HIRING SYSTEM - DATABASE SCHEMA
-- =====================================================
-- Author: Your Name
-- Created: 2024
-- Database: MySQL 8.0+
-- =====================================================

USE smart_hiring_db;

-- =====================================================
-- TABLE: roles
-- Description: Stores user roles (ADMIN, HR, CANDIDATE)
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: users
-- Description: Stores all user accounts
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    profile_picture VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_active (is_active)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: user_roles (Join Table)
-- Description: Many-to-Many relationship between users and roles
-- =====================================================
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                          PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: candidates
-- Description: Extended profile for candidate users
-- =====================================================
CREATE TABLE IF NOT EXISTS candidates (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT NOT NULL UNIQUE,
                                          headline VARCHAR(200),
    summary TEXT,
    skills TEXT,
    total_experience DECIMAL(4,2),
    current_company VARCHAR(100),
    current_designation VARCHAR(100),
    current_salary DECIMAL(12,2),
    expected_salary DECIMAL(12,2),
    notice_period INT,
    location VARCHAR(100),
    preferred_locations VARCHAR(255),
    education VARCHAR(255),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    portfolio_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_candidates_skills (skills(100)),
    INDEX idx_candidates_experience (total_experience),
    INDEX idx_candidates_location (location)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: resumes
-- Description: Stores uploaded resumes and parsed data
-- =====================================================
CREATE TABLE IF NOT EXISTS resumes (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       candidate_id BIGINT NOT NULL,
                                       file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    parsed_text LONGTEXT,
    extracted_skills TEXT,
    extracted_experience TEXT,
    extracted_education TEXT,
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    INDEX idx_resumes_candidate (candidate_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: jobs
-- Description: Job postings created by HR/Admin
-- =====================================================
CREATE TABLE IF NOT EXISTS jobs (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    responsibilities TEXT,
    required_skills TEXT NOT NULL,
    nice_to_have_skills TEXT,
    experience_min INT DEFAULT 0,
    experience_max INT,
    salary_min DECIMAL(12,2),
    salary_max DECIMAL(12,2),
    salary_currency VARCHAR(10) DEFAULT 'INR',
    location VARCHAR(100),
    job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'REMOTE') DEFAULT 'FULL_TIME',
    work_mode ENUM('ONSITE', 'REMOTE', 'HYBRID') DEFAULT 'ONSITE',
    department VARCHAR(100),
    openings INT DEFAULT 1,
    posted_by BIGINT NOT NULL,
    application_deadline DATE,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    views_count INT DEFAULT 0,
    applications_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (posted_by) REFERENCES users(id),
    INDEX idx_jobs_active (is_active),
    INDEX idx_jobs_location (location),
    INDEX idx_jobs_type (job_type),
    INDEX idx_jobs_experience (experience_min, experience_max),
    FULLTEXT INDEX idx_jobs_search (title, description, required_skills)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: applications
-- Description: Job applications by candidates
-- =====================================================
CREATE TABLE IF NOT EXISTS applications (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            job_id BIGINT NOT NULL,
                                            candidate_id BIGINT NOT NULL,
                                            resume_id BIGINT,
                                            cover_letter TEXT,
                                            status ENUM('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED',
                                            'INTERVIEWED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN') DEFAULT 'APPLIED',
    ai_score DECIMAL(5,2),
    ai_feedback TEXT,
    hr_rating INT,
    hr_notes TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,

    UNIQUE KEY unique_application (job_id, candidate_id),
    INDEX idx_applications_status (status),
    INDEX idx_applications_score (ai_score DESC)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: shortlists
-- Description: Shortlisted candidates for jobs
-- =====================================================
CREATE TABLE IF NOT EXISTS shortlists (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          job_id BIGINT NOT NULL,
                                          candidate_id BIGINT NOT NULL,
                                          application_id BIGINT NOT NULL,
                                          shortlisted_by BIGINT NOT NULL,
                                          stage ENUM('INITIAL', 'TECHNICAL', 'HR', 'FINAL') DEFAULT 'INITIAL',
    remarks TEXT,
    interview_date DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (shortlisted_by) REFERENCES users(id),

    UNIQUE KEY unique_shortlist (job_id, candidate_id),
    INDEX idx_shortlists_stage (stage)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: notifications
-- Description: System notifications for users
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('APPLICATION_RECEIVED', 'APPLICATION_STATUS', 'INTERVIEW_SCHEDULED',
              'JOB_RECOMMENDATION', 'PROFILE_UPDATE', 'SYSTEM') DEFAULT 'SYSTEM',
    reference_id BIGINT,
    reference_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_read (is_read),
    INDEX idx_notifications_created (created_at DESC)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: email_logs
-- Description: Log of all emails sent by the system
-- =====================================================
CREATE TABLE IF NOT EXISTS email_logs (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          recipient_email VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    body TEXT,
    template_name VARCHAR(100),
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_email_status (status),
    INDEX idx_email_recipient (recipient_email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- VERIFICATION: Show all tables
-- =====================================================
SHOW TABLES;