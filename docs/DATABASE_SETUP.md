# Database Setup Guide

## Creating the Database

Run the following SQL commands to set up the MySQL database:

```sql
-- Create Database
CREATE DATABASE IF NOT EXISTS eduapp_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE eduapp_db;

-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN', 'DEVELOPER') NOT NULL,
    profile_image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- Courses Table
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT,
    thumbnail_url VARCHAR(500),
    instructor_id BIGINT NOT NULL,
    is_published BOOLEAN DEFAULT FALSE,
    total_lessons INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_instructor (instructor_id),
    INDEX idx_published (is_published)
);

-- Lessons Table
CREATE TABLE lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    video_url VARCHAR(500),
    resource_url VARCHAR(500),
    course_id BIGINT NOT NULL,
    order_index INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course (course_id)
);

-- Quizzes Table
CREATE TABLE quizzes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT,
    lesson_id BIGINT NOT NULL,
    total_questions INT DEFAULT 0,
    passing_score INT DEFAULT 70,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    INDEX idx_lesson (lesson_id)
);

-- Questions Table
CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_text LONGTEXT NOT NULL,
    option_a LONGTEXT,
    option_b LONGTEXT,
    option_c LONGTEXT,
    option_d LONGTEXT,
    correct_answer VARCHAR(1) NOT NULL,
    quiz_id BIGINT NOT NULL,
    marks INT DEFAULT 1,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_quiz (quiz_id)
);

-- User Progress Table
CREATE TABLE user_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    completed_lessons INT DEFAULT 0,
    total_lessons INT DEFAULT 0,
    progress_percentage DOUBLE DEFAULT 0.0,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_course (user_id, course_id),
    INDEX idx_user (user_id),
    INDEX idx_course (course_id)
);

-- Quiz Results Table
CREATE TABLE quiz_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    score INT DEFAULT 0,
    total_marks INT DEFAULT 0,
    percentage DOUBLE DEFAULT 0.0,
    is_passed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_quiz (quiz_id)
);

-- Quiz Assignments Table (New Feature)
CREATE TABLE quiz_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_quiz (user_id, quiz_id),
    INDEX idx_user_quiz (user_id, quiz_id)
);

-- Test Data (Optional)
-- Insert Sample Admin User
INSERT INTO users (email, password, full_name, role, is_active)
VALUES (
    'admin@eduapp.com',
    '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', -- BCrypt hashed 'password123'
    'Admin User',
    'ADMIN',
    TRUE
);

-- Insert Sample Instructor
INSERT INTO users (email, password, full_name, role, is_active)
VALUES (
    'instructor@eduapp.com',
    '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
    'Instructor User',
    'DEVELOPER',
    TRUE
);

-- Insert Sample Student
INSERT INTO users (email, password, full_name, role, is_active)
VALUES (
    'student@eduapp.com',
    '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
    'Student User',
    'STUDENT',
    TRUE
);
```

## Notes

1. **Database Character Set**: Uses `utf8mb4` for full Unicode support
2. **Timestamps**: Automatic creation and update timestamps
3. **Foreign Keys**: Cascading deletes for data integrity
4. **Indexes**: Added on frequently queried columns for performance
5. **Default Values**: Configured appropriate defaults
6. **Not Null Constraints**: Applied where needed

## Running the Script

1. Open MySQL client
2. Copy and paste the SQL commands above
3. Or save as `db-setup.sql` and run: `mysql -u root -p < db-setup.sql`

## Verification

After setup, verify the tables were created:

```sql
USE eduapp_db;
SHOW TABLES;
DESCRIBE users;
```

All tables should be created successfully!
