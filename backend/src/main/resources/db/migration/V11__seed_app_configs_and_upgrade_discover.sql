-- V11: Seed dữ liệu Cấu hình App (Gói PRO, Nhắc nhở) và bảo đảm tính đồng bộ
INSERT INTO app_configs (config_key, config_value, description, updated_by, updated_at) VALUES
('pro_title', 'Gói Nâng Cấp HomeFit PRO ⚡', 'Tiêu đề gói PRO hiển thị trên App', 'system', NOW()),
('pro_benefits', 'Mở khóa tất cả bài tập nâng cao;Không có quảng cáo làm phiền;Tùy chỉnh giáo trình tập luyện cá nhân;Đồng bộ tiến độ đa thiết bị cloud', 'Danh sách quyền lợi gói PRO', 'system', NOW()),
('pro_price', '59.000 VNĐ / tháng', 'Giá gói PRO', 'system', NOW()),
('reminder_message_default', 'Đã đến giờ rèn luyện sức khỏe cùng HomeFit! Hãy dành 15 phút hôm nay nhé 💪', 'Thông báo nhắc tập mặc định', 'system', NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_at = NOW();
