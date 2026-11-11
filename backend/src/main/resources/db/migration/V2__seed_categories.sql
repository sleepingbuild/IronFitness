-- V2: 4 nhóm cơ ban đầu. Sử dụng INSERT IGNORE để tránh lỗi duplicate key nếu CSDL đã tồn tại dữ liệu.
INSERT IGNORE INTO categories (name, slug, icon_url, sort_order, is_active, updated_at) VALUES
    ('Bụng',      'abs',       NULL, 1, TRUE, UTC_TIMESTAMP()),
    ('Ngực',      'chest',     NULL, 2, TRUE, UTC_TIMESTAMP()),
    ('Chân',      'legs',      NULL, 3, TRUE, UTC_TIMESTAMP()),
    ('Toàn thân', 'full_body', NULL, 4, TRUE, UTC_TIMESTAMP());
