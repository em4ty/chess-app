-- Начальная схема. Скрипт идемпотентный (IF NOT EXISTS), поэтому он безопасно
-- применяется и к пустой БД, и к БД, которую раньше создал Hibernate (ddl-auto=update).

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    email         VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Логин и email не зависят от регистра: "Ivan" и "ivan" - один и тот же пользователь
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username_lower ON users (lower(username));
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_lower    ON users (lower(email)) WHERE email IS NOT NULL;

CREATE TABLE IF NOT EXISTS user_statistics (
    id                     BIGSERIAL PRIMARY KEY,
    user_id                BIGINT    NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    correct_answers        INTEGER   NOT NULL DEFAULT 0 CHECK (correct_answers BETWEEN 0 AND 15),
    exercises_completed    INTEGER   NOT NULL DEFAULT 0 CHECK (exercises_completed BETWEEN 0 AND 15),
    total_progress_percent INTEGER   NOT NULL DEFAULT 0 CHECK (total_progress_percent BETWEEN 0 AND 100),
    last_updated           TIMESTAMP NOT NULL DEFAULT now()
);

-- Какие именно упражнения пройдены. Уникальность (пользователь, тип, уровень)
-- не даёт "накрутить" счётчик повторным прохождением одного и того же упражнения.
CREATE TABLE IF NOT EXISTS completed_exercises (
    id            BIGSERIAL   PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exercise_type VARCHAR(20) NOT NULL CHECK (exercise_type IN ('defense', 'attack', 'checkmate')),
    level         INTEGER     NOT NULL CHECK (level BETWEEN 1 AND 5),
    completed_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_completed_exercise UNIQUE (user_id, exercise_type, level)
);
