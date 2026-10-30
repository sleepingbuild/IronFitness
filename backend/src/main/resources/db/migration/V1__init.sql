-- V1: schema ban đầu theo docs/DATABASE.md
-- Charset utf8mb4, thời gian lưu UTC (DATETIME).

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NULL COMMENT 'NULL nếu chỉ đăng nhập Google',
    google_id     VARCHAR(64)  NULL,
    display_name  VARCHAR(100) NOT NULL DEFAULT '',
    role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    is_premium    BOOLEAN      NOT NULL DEFAULT FALSE,
    height_cm     DECIMAL(5,1) NULL,
    weight_kg     DECIMAL(5,1) NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_google_id (google_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(100) NOT NULL,
    icon_url   VARCHAR(500) NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'xóa mềm',
    updated_at DATETIME     NOT NULL COMMENT 'dùng cho đồng bộ updatedAfter',
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_slug (slug),
    KEY idx_categories_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE exercises (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    category_id  BIGINT       NOT NULL,
    name         VARCHAR(150) NOT NULL,
    description  TEXT         NULL,
    media_url    VARCHAR(500) NOT NULL,
    media_type   ENUM('GIF','VIDEO') NOT NULL,
    duration_sec INT          NULL COMMENT 'tập theo thời gian',
    reps         INT          NULL COMMENT 'tập theo số lần',
    rest_sec     INT          NOT NULL DEFAULT 15,
    sort_order   INT          NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'xóa mềm',
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_exercises_category (category_id),
    KEY idx_exercises_updated_at (updated_at),
    CONSTRAINT fk_exercises_category FOREIGN KEY (category_id) REFERENCES categories (id),
    -- Đúng một trong duration_sec hoặc reps có giá trị
    CONSTRAINT chk_exercises_mode CHECK ((duration_sec IS NULL) <> (reps IS NULL)),
    CONSTRAINT chk_exercises_values CHECK (
        (duration_sec IS NULL OR duration_sec > 0)
        AND (reps IS NULL OR reps > 0)
        AND rest_sec >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE workout_sessions (
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    user_id        BIGINT   NOT NULL,
    client_uuid    CHAR(36) NOT NULL COMMENT 'do app sinh, chống trùng',
    category_id    BIGINT   NULL,
    started_at     DATETIME NOT NULL,
    completed_at   DATETIME NOT NULL,
    total_seconds  INT      NOT NULL,
    exercises_done INT      NOT NULL,
    created_at     DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sessions_user_uuid (user_id, client_uuid),
    KEY idx_sessions_completed_at (completed_at),
    KEY idx_sessions_category (category_id),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_sessions_values CHECK (total_seconds >= 0 AND exercises_done >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL,
    token_hash CHAR(64) NOT NULL COMMENT 'SHA-256 dạng hex, không lưu token gốc',
    expires_at DATETIME NOT NULL,
    revoked    BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
