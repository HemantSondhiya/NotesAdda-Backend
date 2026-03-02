CREATE TABLE IF NOT EXISTS roles (
    role_id INT NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(20),
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_role_name (role_name)
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(120) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
);

CREATE TABLE IF NOT EXISTS universities (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(20) NOT NULL,
    city VARCHAR(255),
    state VARCHAR(255),
    logo_url VARCHAR(255),
    is_active BIT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_universities_code (code)
);

CREATE TABLE IF NOT EXISTS programs (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    duration SMALLINT,
    university_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_program_name_university (name, university_id),
    KEY idx_programs_university_id (university_id),
    CONSTRAINT fk_programs_university FOREIGN KEY (university_id) REFERENCES universities (id)
);

CREATE TABLE IF NOT EXISTS branches (
    id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) NOT NULL,
    program_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_branch_name_program (name, program_id),
    KEY idx_branches_program_id (program_id),
    CONSTRAINT fk_branches_program FOREIGN KEY (program_id) REFERENCES programs (id)
);

CREATE TABLE IF NOT EXISTS semesters (
    id BINARY(16) NOT NULL,
    number SMALLINT NOT NULL,
    branch_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_semester_number_branch (number, branch_id),
    KEY idx_semesters_branch_id (branch_id),
    CONSTRAINT fk_semesters_branch FOREIGN KEY (branch_id) REFERENCES branches (id)
);

CREATE TABLE IF NOT EXISTS subjects (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(30),
    credits SMALLINT,
    syllabus_url VARCHAR(255),
    semester_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subject_code_semester (code, semester_id),
    KEY idx_subjects_semester_id (semester_id),
    CONSTRAINT fk_subjects_semester FOREIGN KEY (semester_id) REFERENCES semesters (id)
);

CREATE TABLE IF NOT EXISTS notes (
    id BINARY(16) NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    file_url VARCHAR(255) NOT NULL,
    file_key VARCHAR(255) NOT NULL,
    file_type VARCHAR(20),
    is_approved BIT,
    rejection_note VARCHAR(255),
    created_at DATETIME(6),
    approved_at DATETIME(6),
    subject_id BINARY(16) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    approved_by BIGINT,
    PRIMARY KEY (id),
    KEY idx_notes_subject_id (subject_id),
    KEY idx_notes_uploaded_by (uploaded_by),
    KEY idx_notes_approved_by (approved_by),
    CONSTRAINT fk_notes_subject FOREIGN KEY (subject_id) REFERENCES subjects (id),
    CONSTRAINT fk_notes_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (user_id),
    CONSTRAINT fk_notes_approved_by FOREIGN KEY (approved_by) REFERENCES users (user_id)
);
