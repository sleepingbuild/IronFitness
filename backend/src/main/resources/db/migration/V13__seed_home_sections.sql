-- V13: Bổ sung các Section Trang chủ còn thiếu trên App
INSERT INTO home_sections (section_key, title_vi, section_type, sort_order, is_enabled, created_at) VALUES
('short_sessions', 'Buổi ngắn (< 15 phút)', 'quick_filter', 5, TRUE, NOW()),
('popular_goals', 'Mục tiêu phổ biến (Cơ bắp - Đốt mỡ - Giữ dáng)', 'goals', 6, TRUE, NOW())
ON DUPLICATE KEY UPDATE title_vi = VALUES(title_vi), sort_order = VALUES(sort_order);
