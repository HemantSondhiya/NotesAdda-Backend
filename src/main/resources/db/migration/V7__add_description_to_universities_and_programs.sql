ALTER TABLE universities
    ADD COLUMN description TEXT NULL AFTER code;

ALTER TABLE programs
    ADD COLUMN description TEXT NULL AFTER name;
