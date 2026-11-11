-- V7: Bảng Mục Khám Phá (Discover) và Seed dữ liệu chuẩn khớp 100% với Mobile App

CREATE TABLE IF NOT EXISTS discover_items (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title_vi    VARCHAR(200) NOT NULL,
    title_en    VARCHAR(200) NULL,
    description TEXT         NULL,
    category    VARCHAR(50)  NOT NULL DEFAULT 'Góc Kiến Thức & Mẹo',
    cover_url   VARCHAR(500) NOT NULL,
    action_url  VARCHAR(500) NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_discover_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Làm sạch và nạp lại dữ liệu Khám Phá khớp 100% với giao diện Mobile App
DELETE FROM discover_items;

INSERT INTO discover_items (title_vi, title_en, description, category, cover_url, action_url, sort_order, is_active, updated_at) VALUES
-- 1. Banner Giới thiệu App
('HomeFit Fitness v2.5.0 PRO', 'HomeFit Fitness PRO', 'Ứng dụng luyện tập cá nhân hóa tại nhà hàng đầu. ✨ Tùy chỉnh bài tập linh hoạt ✨ Đếm ngược tự động ✨ Báo cáo BMI chuẩn xác.', 'Giới thiệu App', 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600', '/pro', 1, TRUE, NOW()),

-- 2. Góc Kiến Thức & Mẹo Tập Luyện
('Bí quyết ăn uống cân bằng tăng 200% hiệu quả tập', 'Balanced Diet Tips', 'Kết hợp đạm và xơ hợp lý giúp cơ bắp phục hồi nhanh hơn sau mỗi buổi tập.', 'Góc Kiến Thức & Mẹo', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=600', '/nutrition', 2, TRUE, NOW()),
('Tầm quan trọng của giấc ngủ & kéo giãn cơ', 'Sleep & Recovery', 'Kéo giãn cơ 5 phút sau buổi tập giúp tránh chấn thương và giảm mỏi cơ đáng kể.', 'Góc Kiến Thức & Mẹo', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600', '/recovery', 3, TRUE, NOW()),
('Cộng Đồng HomeFit 100.000+ Hội Viên', 'HomeFit Community', 'Chia sẻ tiến độ, động lực và hình ảnh thay đổi vóc dáng cùng cộng đồng Việt Nam.', 'Góc Kiến Thức & Mẹo', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600', '/community', 4, TRUE, NOW()),

-- 3. Hệ Sinh Thái Dự Án HomeFit
('HomeFit Nutrition', 'HomeFit Nutrition App', 'Tính Calo thực đơn Eat Clean & theo dõi dinh dưỡng hằng ngày.', 'Hệ Sinh Thái Dự Án', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=600', 'https://play.google.com/store/apps/details?id=com.homefit.nutrition', 5, TRUE, NOW()),
('HomeFit Yoga & Meditate', 'HomeFit Yoga App', 'Bài tập Yoga 15 phút giúp dẻo dai & thư giãn tâm trí sau ngày làm việc.', 'Hệ Sinh Thái Dự Án', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600', 'https://play.google.com/store/apps/details?id=com.homefit.yoga', 6, TRUE, NOW()),
('HomeFit Running Tracker', 'HomeFit Running App', 'Đo quãng đường chạy GPS, tốc độ & lượng Calo tiêu thụ chuẩn xác.', 'Hệ Sinh Thái Dự Án', 'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=600', 'https://play.google.com/store/apps/details?id=com.homefit.running', 7, TRUE, NOW());

-- Seed Home Sections mẫu nếu chưa có
INSERT INTO home_sections (section_key, title_vi, section_type, sort_order, is_enabled, created_at) VALUES
('hero_challenge', 'Thử thách 28 Ngày', 'hero', 1, TRUE, NOW()),
('body_parts', 'Cơ thể tập trung', 'body_parts', 2, TRUE, NOW()),
('quick_filters', 'Bộ lọc nhanh', 'quick_filter', 3, TRUE, NOW()),
('recommended', 'Được khuyến nghị cho bạn', 'recommended', 4, TRUE, NOW())
ON DUPLICATE KEY UPDATE title_vi = VALUES(title_vi);
