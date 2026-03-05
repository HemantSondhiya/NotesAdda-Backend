CREATE TABLE IF NOT EXISTS pending_user_registration (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_sent_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pending_user_registration_username (username),
    UNIQUE KEY uk_pending_user_registration_email (email)
);
