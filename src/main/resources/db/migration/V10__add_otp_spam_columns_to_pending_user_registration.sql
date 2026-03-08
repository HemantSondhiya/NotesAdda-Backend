ALTER TABLE pending_user_registration
    ADD COLUMN send_count INT NOT NULL DEFAULT 0,
    ADD COLUMN send_count_reset_at DATETIME NULL;