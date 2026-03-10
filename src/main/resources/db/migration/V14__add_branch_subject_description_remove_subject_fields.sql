ALTER TABLE branches
    ADD COLUMN description VARCHAR(1000);

ALTER TABLE subjects
    ADD COLUMN description VARCHAR(1000);

ALTER TABLE subjects
    DROP COLUMN credits,
    DROP COLUMN syllabus_url;
