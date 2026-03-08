-- V13__change_user_id_to_uuid.sql
-- WARNING: This is a DESTRUCTIVE migration taking the easiest path to migrate from BIGINT to UUID by dropping and recreating tables.

-- 1. Drop existing FKs from dependent tables that we aren't completely dropping
ALTER TABLE notes DROP FOREIGN KEY fk_notes_uploaded_by;
ALTER TABLE notes DROP FOREIGN KEY fk_notes_approved_by;

-- 2. Disable Foreign Key checks to drop and modify easily
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS email_verification_otp;
DROP TABLE IF EXISTS pending_user_registration;
DROP TABLE IF EXISTS users;

-- 3. Recreate Users table with UUID
-- 2. Recreate Users table with UUID
CREATE TABLE users (
    user_id VARCHAR(36) NOT NULL,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id)
);

-- 4. Recreate dependent Auth tables with UUID
CREATE TABLE user_role (
    user_id VARCHAR(36) NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
);

CREATE TABLE pending_user_registration (
    id VARCHAR(36) NOT NULL,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempt_count INT NOT NULL,
    last_sent_at DATETIME(6) NOT NULL,
    send_count INT NOT NULL DEFAULT 0,
    send_count_reset_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE password_reset_token (
    id VARCHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    expires_at DATETIME(6) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE email_verification_otp (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_sent_at DATETIME(6) NOT NULL,
    verified_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 5. Clean up dirty notes and update table references
DELETE FROM notes;

ALTER TABLE notes MODIFY COLUMN uploaded_by VARCHAR(36) NOT NULL;
ALTER TABLE notes MODIFY COLUMN approved_by VARCHAR(36);

ALTER TABLE notes ADD CONSTRAINT fk_notes_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (user_id);
ALTER TABLE notes ADD CONSTRAINT fk_notes_approved_by FOREIGN KEY (approved_by) REFERENCES users (user_id);

-- 6. Re-enable Foreign Key checks
SET FOREIGN_KEY_CHECKS=1;
