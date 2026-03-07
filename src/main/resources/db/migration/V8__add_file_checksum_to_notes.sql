ALTER TABLE notes ADD COLUMN file_checksum VARCHAR(64) DEFAULT NULL;
CREATE INDEX idx_notes_checksum ON notes (file_checksum);
