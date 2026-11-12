-- V12: Nạp dữ liệu Buổi tập mẫu (Workouts) khớp 100% với các gói buổi tập trên Mobile App
INSERT INTO workouts (type, title_vi, primary_muscle, level, goal, work_sec, rest_sec, round_rest_sec, rounds, equipment, sort_order, status, version, created_at, updated_at) VALUES
-- 1. Theo nhóm cơ (Cơ Bụng / Ngực / Chân / Toàn thân)
('body_part', 'Cơ Bụng Cấp Dễ', 'Bụng', 'easy', 'abs', 30, 15, 30, 1, 'none', 1, 'PUBLISHED', 1, NOW(), NOW()),
('body_part', 'Cơ Bụng Cấp Trung Bình', 'Bụng', 'medium', 'abs', 30, 15, 30, 1, 'none', 2, 'PUBLISHED', 1, NOW(), NOW()),
('body_part', 'Cơ Bụng Cấp Khó', 'Bụng', 'hard', 'abs', 30, 15, 30, 1, 'none', 3, 'PUBLISHED', 1, NOW(), NOW()),
('body_part', 'Ngực & Tay Cấp Trung Bình', 'Ngực', 'medium', 'chest', 30, 15, 30, 1, 'none', 4, 'PUBLISHED', 1, NOW(), NOW()),
('body_part', 'Chân & Mông Cấp Trung Bình', 'Chân', 'medium', 'legs', 30, 15, 30, 1, 'none', 5, 'PUBLISHED', 1, NOW(), NOW()),
('body_part', 'Thân Trên + Lõi Cấp Trung Bình', 'Toàn thân', 'medium', 'full_body', 30, 15, 30, 1, 'none', 6, 'PUBLISHED', 1, NOW(), NOW()),

-- 2. Buổi ngắn (Short Sessions)
('tabata', '6 Phút Với Tabata Cường Độ Cao', 'Toàn thân', 'medium', 'tabata', 20, 10, 20, 1, 'none', 7, 'PUBLISHED', 1, NOW(), NOW()),
('stretch', 'Kéo Giãn Lưng & Giảm Mỏi Cột Sống', 'Lưng', 'easy', 'stretch', 30, 10, 20, 1, 'none', 8, 'PUBLISHED', 1, NOW(), NOW()),

-- 3. Mục tiêu phổ biến (Popular Goals: fatloss, muscle, keepfit)
('goal', 'Đốt Cháy Calo Toàn Thân Cấp Khó', 'Toàn thân', 'hard', 'fatloss', 30, 15, 30, 1, 'none', 9, 'PUBLISHED', 1, NOW(), NOW()),
('goal', 'Thử Thách Sức Mạnh Chân & Mông', 'Chân', 'medium', 'muscle', 30, 15, 30, 1, 'none', 10, 'PUBLISHED', 1, NOW(), NOW()),
('goal', 'Giữ Dáng Cân Đối Dễ Nhàng Hằng Ngày', 'Toàn thân', 'easy', 'keepfit', 30, 15, 30, 1, 'none', 11, 'PUBLISHED', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE title_vi = VALUES(title_vi), updated_at = NOW();
