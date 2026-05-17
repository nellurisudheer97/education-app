CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    plain_password VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_email ON users(email);

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    instructor_id BIGINT NOT NULL,
    is_published BOOLEAN DEFAULT FALSE,
    total_lessons INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_instructor
        FOREIGN KEY (instructor_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_instructor ON courses(instructor_id);
CREATE INDEX idx_published ON courses(is_published);

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    video_url VARCHAR(500),
    resource_url VARCHAR(500),
    course_id BIGINT NOT NULL,
    order_index INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_course ON lessons(course_id);

CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    lesson_id BIGINT NOT NULL,
    total_questions INT DEFAULT 0,
    passing_score INT DEFAULT 70,
    timer_minutes INT DEFAULT 30,
    total_marks INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lesson
        FOREIGN KEY (lesson_id)
        REFERENCES lessons(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_lesson ON quizzes(lesson_id);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL DEFAULT 'MCQ',
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_answer VARCHAR(255),
    sample_answer TEXT,
    quiz_id BIGINT NOT NULL,
    marks INT DEFAULT 1,
    order_index INT DEFAULT 0,
    CONSTRAINT fk_quiz
        FOREIGN KEY (quiz_id)
        REFERENCES quizzes(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_quiz ON questions(quiz_id);

CREATE TABLE user_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    completed_lessons INT DEFAULT 0,
    total_lessons INT DEFAULT 0,
    progress_percentage DOUBLE PRECISION DEFAULT 0.0,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_progress_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,
    CONSTRAINT unique_user_course
        UNIQUE(user_id, course_id)
);
CREATE INDEX idx_progress_user ON user_progress(user_id);
CREATE INDEX idx_progress_course ON user_progress(course_id);

CREATE TABLE quiz_results (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    score INT DEFAULT 0,
    total_marks INT DEFAULT 0,
    percentage DOUBLE PRECISION DEFAULT 0.0,
    is_passed BOOLEAN DEFAULT FALSE,
    grade VARCHAR(50) DEFAULT 'PENDING',
    status VARCHAR(50) DEFAULT 'SUBMITTED',
    time_taken_seconds INT DEFAULT 0,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_result_quiz
        FOREIGN KEY (quiz_id)
        REFERENCES quizzes(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_result_user ON quiz_results(user_id);
CREATE INDEX idx_result_quiz ON quiz_results(quiz_id);

CREATE TABLE quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    result_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option VARCHAR(255),
    text_answer TEXT,
    awarded_marks INT DEFAULT 0,
    max_marks INT DEFAULT 0,
    is_correct BOOLEAN DEFAULT FALSE,
    reviewed BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_answer_result
        FOREIGN KEY (result_id)
        REFERENCES quiz_results(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_answer_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_answer_result ON quiz_answers(result_id);
CREATE INDEX idx_answer_question ON quiz_answers(question_id);

CREATE TABLE quiz_assignments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_assignment_quiz
        FOREIGN KEY (quiz_id)
        REFERENCES quizzes(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_assignment_user ON quiz_assignments(user_id);
CREATE INDEX idx_assignment_quiz ON quiz_assignments(quiz_id);

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_token ON password_reset_tokens(token);
CREATE INDEX idx_token_user ON password_reset_tokens(user_id);

INSERT INTO users (email, password, full_name, role, is_active)
VALUES ('admin@eduapp.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', 'Admin User', 'ADMIN', TRUE);

INSERT INTO users (email, password, full_name, role, is_active)
VALUES ('instructor@eduapp.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', 'Instructor User', 'DEVELOPER', TRUE);

INSERT INTO users (email, password, full_name, role, is_active)
VALUES ('student@eduapp.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', 'Student User', 'STUDENT', TRUE);
