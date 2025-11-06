CREATE TABLE IF NOT EXISTS student_data (
    student_number BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    joining_date DATE NOT NULL,
    student_status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS course_details (
    course_number BIGSERIAL PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    course_credits DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS address_details (
    address_number BIGSERIAL PRIMARY KEY,
    student_number BIGINT NOT NULL,
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    CONSTRAINT fk_address_student FOREIGN KEY (student_number) REFERENCES student_data(student_number)
);

CREATE TABLE IF NOT EXISTS contact_details (
    contact_number BIGSERIAL PRIMARY KEY,
    student_number BIGINT NOT NULL,
    email_address VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(15) NOT NULL,
    CONSTRAINT fk_contact_student FOREIGN KEY (student_number) REFERENCES student_data(student_number)
);

CREATE TABLE IF NOT EXISTS enrollment_data (
    enrollment_number BIGSERIAL PRIMARY KEY,
    student_number BIGINT NOT NULL,
    course_number BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    overall_grade INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    instructor_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_number) REFERENCES student_data(student_number),
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_number) REFERENCES course_details(course_number)
);

CREATE TABLE IF NOT EXISTS attendance_data (
    attendance_number BIGSERIAL PRIMARY KEY,
    enrollment_number BIGINT NOT NULL,
    student_number BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    attendance_status VARCHAR(10) NOT NULL,
    semester VARCHAR(20),
    CONSTRAINT fk_attendance_enrollment FOREIGN KEY (enrollment_number) REFERENCES enrollment_data(enrollment_number),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_number) REFERENCES student_data(student_number)
);

CREATE TABLE IF NOT EXISTS grades (
    grade_number BIGSERIAL PRIMARY KEY,
    enrollment_number BIGINT NOT NULL,
    assessment_date DATE NOT NULL,
    assessment_type VARCHAR(50) NOT NULL,
    obtained_score INTEGER NOT NULL,
    max_score INTEGER NOT NULL DEFAULT 100,
    grade_code INTEGER NOT NULL,
    CONSTRAINT fk_grades_enrollment FOREIGN KEY (enrollment_number) REFERENCES enrollment_data(enrollment_number)
);

