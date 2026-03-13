-- Allow duplicate slugs for branches
DROP INDEX idx_branches_slug ON branches;
CREATE INDEX idx_branches_slug ON branches(slug);
