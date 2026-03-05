ALTER TABLE users
    ADD COLUMN email_verified BIT NOT NULL DEFAULT b'1';

CREATE TABLE IF NOT EXISTS email_verification_otp (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_sent_at DATETIME(6) NOT NULL,
    verified_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verification_otp_user (user_id),
    CONSTRAINT fk_email_verification_otp_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);
