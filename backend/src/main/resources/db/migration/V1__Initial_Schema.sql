-- ============================================================
-- V1__Initial_Schema.sql
-- Flyway initial migration for the Educational Platform
-- Creates all tables, indexes, and seeds sample data
-- ============================================================

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id               BIGSERIAL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    plain_password   VARCHAR(255),
    full_name        VARCHAR(255) NOT NULL,
    role             VARCHAR(50)  NOT NULL DEFAULT 'STUDENT',
    profile_image_url VARCHAR(500),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP             DEFAULT NOW(),

    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'INSTRUCTOR', 'ADMIN', 'DEVELOPER'))
);

CREATE INDEX IF NOT EXISTS idx_users_email  ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role   ON users (role);

-- ============================================================
-- COURSES
-- ============================================================
CREATE TABLE IF NOT EXISTS courses (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    thumbnail_url   VARCHAR(500),
    instructor_id   BIGINT       NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    is_published    BOOLEAN      NOT NULL DEFAULT FALSE,
    total_lessons   INTEGER               DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_courses_instructor ON courses (instructor_id);
CREATE INDEX IF NOT EXISTS idx_courses_published  ON courses (is_published);

-- ============================================================
-- LESSONS
-- ============================================================
CREATE TABLE IF NOT EXISTS lessons (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT,
    video_url    VARCHAR(500),
    resource_url VARCHAR(500),
    course_id    BIGINT       NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    order_index  INTEGER               DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lessons_course ON lessons (course_id);

-- ============================================================
-- QUIZZES
-- ============================================================
CREATE TABLE IF NOT EXISTS quizzes (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    lesson_id       BIGINT       NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    total_questions INTEGER               DEFAULT 0,
    passing_score   INTEGER               DEFAULT 70,
    timer_minutes   INTEGER               DEFAULT 30,
    total_marks     INTEGER               DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quizzes_lesson ON quizzes (lesson_id);

-- ============================================================
-- QUESTIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS questions (
    id            BIGSERIAL PRIMARY KEY,
    question_text TEXT         NOT NULL,
    question_type VARCHAR(50)  NOT NULL DEFAULT 'MCQ',
    option_a      TEXT,
    option_b      TEXT,
    option_c      TEXT,
    option_d      TEXT,
    correct_answer VARCHAR(255),
    sample_answer TEXT,
    quiz_id       BIGINT       NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    marks         INTEGER               DEFAULT 1,
    order_index   INTEGER               DEFAULT 0,

    CONSTRAINT chk_questions_type CHECK (question_type IN ('MCQ', 'DESCRIPTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_questions_quiz ON questions (quiz_id);

-- ============================================================
-- USER_PROGRESS
-- ============================================================
CREATE TABLE IF NOT EXISTS user_progress (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id           BIGINT           NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    completed_lessons   INTEGER                   DEFAULT 0,
    total_lessons       INTEGER                   DEFAULT 0,
    progress_percentage DOUBLE PRECISION          DEFAULT 0.0,
    enrolled_at         TIMESTAMP        NOT NULL DEFAULT NOW(),
    last_accessed_at    TIMESTAMP                 DEFAULT NOW(),

    CONSTRAINT uq_user_progress UNIQUE (user_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_user_progress_user   ON user_progress (user_id);
CREATE INDEX IF NOT EXISTS idx_user_progress_course ON user_progress (course_id);

-- ============================================================
-- QUIZ_RESULTS
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_results (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    quiz_id            BIGINT           NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    score              INTEGER                   DEFAULT 0,
    total_marks        INTEGER                   DEFAULT 0,
    percentage         DOUBLE PRECISION          DEFAULT 0.0,
    is_passed          BOOLEAN                   DEFAULT FALSE,
    grade              VARCHAR(20)               DEFAULT 'PENDING',
    status             VARCHAR(20)               DEFAULT 'SUBMITTED',
    time_taken_seconds INTEGER                   DEFAULT 0,
    completed_at       TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_results_user ON quiz_results (user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_results_quiz ON quiz_results (quiz_id);

-- ============================================================
-- QUIZ_ANSWERS
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_answers (
    id              BIGSERIAL PRIMARY KEY,
    result_id       BIGINT       NOT NULL REFERENCES quiz_results (id) ON DELETE CASCADE,
    question_id     BIGINT       NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    selected_option VARCHAR(255),
    text_answer     TEXT,
    awarded_marks   INTEGER               DEFAULT 0,
    max_marks       INTEGER               DEFAULT 0,
    is_correct      BOOLEAN               DEFAULT FALSE,
    reviewed        BOOLEAN               DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_quiz_answers_result   ON quiz_answers (result_id);
CREATE INDEX IF NOT EXISTS idx_quiz_answers_question ON quiz_answers (question_id);

-- ============================================================
-- QUIZ_ASSIGNMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_assignments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    quiz_id     BIGINT    NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_quiz_assignment UNIQUE (user_id, quiz_id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_assignments_user ON quiz_assignments (user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_assignments_quiz ON quiz_assignments (quiz_id);

-- ============================================================
-- PASSWORD_RESET_TOKENS
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_prt_token   ON password_reset_tokens (token);
CREATE INDEX IF NOT EXISTS idx_prt_user_id ON password_reset_tokens (user_id);

-- ============================================================
-- SAMPLE DATA
-- Passwords are BCrypt hashes of 'password123'
-- ============================================================
INSERT INTO users (email, password, full_name, role, is_active, created_at, updated_at)
VALUES
    (
        'admin@eduapp.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Admin User',
        'ADMIN',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'instructor@eduapp.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Jane Instructor',
        'INSTRUCTOR',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'student@eduapp.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'John Student',
        'STUDENT',
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO NOTHING;
