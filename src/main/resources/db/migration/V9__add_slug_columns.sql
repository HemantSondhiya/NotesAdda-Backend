-- Add slug columns to all entity tables
ALTER TABLE universities ADD COLUMN slug VARCHAR(255);
ALTER TABLE programs ADD COLUMN slug VARCHAR(255);
ALTER TABLE branches ADD COLUMN slug VARCHAR(255);
ALTER TABLE subjects ADD COLUMN slug VARCHAR(255);
ALTER TABLE notes ADD COLUMN slug VARCHAR(255);

-- Unique indexes (slug must be unique per table)
CREATE UNIQUE INDEX idx_universities_slug ON universities(slug);
CREATE UNIQUE INDEX idx_programs_slug ON programs(slug);
CREATE UNIQUE INDEX idx_branches_slug ON branches(slug);
CREATE UNIQUE INDEX idx_subjects_slug ON subjects(slug);
CREATE UNIQUE INDEX idx_notes_slug ON notes(slug);
