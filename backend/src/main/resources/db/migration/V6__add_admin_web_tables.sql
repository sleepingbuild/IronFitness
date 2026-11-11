-- V6: Các bảng hỗ trợ Admin Web (Buổi tập, Thử thách 28 ngày, Trang chủ, Releases, Remote Config, Audit Logs)

CREATE TABLE IF NOT EXISTS workouts (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    type           VARCHAR(50)  NOT NULL COMMENT 'body_part, timed, goal, tabata, stretch, signature',
    title_vi       VARCHAR(200) NOT NULL,
    title_en       VARCHAR(200) NULL,
    primary_muscle VARCHAR(50)  NOT NULL,
    level          VARCHAR(20)  NOT NULL COMMENT 'easy, medium, hard',
    goal           VARCHAR(50)  NULL,
    work_sec       INT          NOT NULL DEFAULT 30,
    rest_sec       INT          NOT NULL DEFAULT 15,
    round_rest_sec INT          NOT NULL DEFAULT 30,
    rounds         INT          NOT NULL DEFAULT 1,
    equipment      VARCHAR(50)  NOT NULL DEFAULT 'none',
    sort_order     INT          NOT NULL DEFAULT 0,
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT, PENDING, PUBLISHED, ARCHIVED',
    version        INT          NOT NULL DEFAULT 1,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_workouts_type (type),
    KEY idx_workouts_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workout_items (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    workout_id     BIGINT      NOT NULL,
    block          VARCHAR(10) NOT NULL COMMENT 'A (warmup), B (main), C (cooldown)',
    position       INT         NOT NULL DEFAULT 1,
    exercise_id    BIGINT      NOT NULL,
    round_num      INT         NOT NULL DEFAULT 1,
    measure        VARCHAR(20) NOT NULL COMMENT 'time, reps',
    value          INT         NOT NULL COMMENT 'số giây hoặc số lần',
    rest_after_sec INT         NOT NULL DEFAULT 15,
    PRIMARY KEY (id),
    KEY idx_workout_items_workout (workout_id),
    CONSTRAINT fk_workout_items_workout FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE,
    CONSTRAINT fk_workout_items_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS challenge_programs (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    level       VARCHAR(20) NOT NULL COMMENT 'easy, medium, hard',
    version     INT         NOT NULL DEFAULT 1,
    status      VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    config_json TEXT        NULL,
    created_at  DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_challenge_level_ver (level, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS challenge_sessions (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    program_id  BIGINT      NOT NULL,
    week_num    INT         NOT NULL COMMENT '1 đến 4',
    day_index   INT         NOT NULL COMMENT '1 đến 28',
    is_rest_day BOOLEAN     NOT NULL DEFAULT FALSE,
    workout_id  BIGINT      NULL,
    PRIMARY KEY (id),
    KEY idx_challenge_sessions_program (program_id),
    CONSTRAINT fk_challenge_sessions_program FOREIGN KEY (program_id) REFERENCES challenge_programs (id) ON DELETE CASCADE,
    CONSTRAINT fk_challenge_sessions_workout FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS home_sections (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    section_key    VARCHAR(50)  NOT NULL UNIQUE,
    title_vi       VARCHAR(150) NOT NULL,
    section_type   VARCHAR(50)  NOT NULL COMMENT 'hero, body_parts, quick_filter, recommended, goals',
    sort_order     INT          NOT NULL DEFAULT 0,
    is_enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME     NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_releases (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    version         INT          NOT NULL UNIQUE,
    notes           VARCHAR(500) NULL,
    manifest_json   LONGTEXT     NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    created_by      VARCHAR(100) NOT NULL,
    published_at    DATETIME     NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_configs (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    config_key   VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT         NOT NULL,
    description  VARCHAR(255) NULL,
    updated_by   VARCHAR(100) NOT NULL DEFAULT 'system',
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    admin_email VARCHAR(100) NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    entity_name VARCHAR(50)  NOT NULL,
    entity_id   VARCHAR(100) NULL,
    detail      TEXT         NULL,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
