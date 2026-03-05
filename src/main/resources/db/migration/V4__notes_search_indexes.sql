CREATE INDEX idx_notes_approved_created_at ON notes (is_approved, created_at DESC);
CREATE INDEX idx_notes_approved_title ON notes (is_approved, title);
