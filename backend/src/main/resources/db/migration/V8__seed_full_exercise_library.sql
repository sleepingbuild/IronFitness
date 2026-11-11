-- V8: Nạp đầy đủ bộ dữ liệu Bài tập & Nhóm cơ theo tài liệu Huong_Dan_Trich_Xuat_DATA.md và danh sách bài tập App

-- Kích hoạt lại toàn bộ danh mục và bài tập
UPDATE categories SET is_active = TRUE, updated_at = NOW();
UPDATE exercises SET is_active = TRUE, updated_at = NOW();

-- Bổ sung thêm các Nhóm cơ còn thiếu (Tay, Lưng)
INSERT INTO categories (name, slug, icon_url, sort_order, is_active, updated_at) VALUES
    ('Tay',  'arms', NULL, 5, TRUE, UTC_TIMESTAMP()),
    ('Lưng', 'back', NULL, 6, TRUE, UTC_TIMESTAMP())
ON DUPLICATE KEY UPDATE is_active = TRUE, updated_at = NOW();

-- Nạp bộ bài tập hoàn chỉnh cho Nhóm cơ Bụng (ABS)
INSERT INTO exercises (category_id, name, description, media_type, media_url, duration_sec, reps, rest_sec, sort_order, is_active, updated_at) VALUES
((SELECT id FROM categories WHERE slug='abs'), 'Gập bụng cơ bản (Crunches)', 'Nằm ngửa, co gối, siết cơ bụng nâng vai khỏi sàn rồi hạ xuống từ từ.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 15, 15, 10, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Plank khuỷu tay', 'Giữ thân người thẳng tựa trên khuỷu tay và mũi chân, siết chặt bụng.', 'GIF', 'https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=600', 45, NULL, 15, 11, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Plank cao giữ thẳng tay', 'Ở tư thế chống đẩy, giữ thẳng tay và siết chặt cơ lõi.', 'GIF', 'https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=600', 45, NULL, 15, 12, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Gập bụng đạp xe (Bicycle Crunches)', 'Nằm ngửa, đan tay sau đầu, xoay người đưa khuỷu tay chạm gối đối diện.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 20, 15, 13, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Nâng chân nằm ngửa (Leg Raise)', 'Nằm ngửa, giữ chân thẳng và nâng vuông góc với sàn rồi hạ chậm.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 12, 20, 14, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Bọ chết (Dead Bug)', 'Nằm ngửa, hạ đồng thời tay trái và chân phải ra xa rồi thu về.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 12, 15, 15, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Chạm gót chân (Heel Taps)', 'Nằm ngửa, nghiêng người lần lượt dùng tay chạm vào gót chân cùng bên.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', 30, NULL, 15, 16, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Gập bụng ngược (Reverse Crunch)', 'Nằm ngửa, cuộn gối kéo hông lên cao rồi hạ xuống chậm.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 12, 15, 17, TRUE, NOW());

-- Nạp bộ bài tập hoàn chỉnh cho Nhóm cơ Ngực (CHEST)
INSERT INTO exercises (category_id, name, description, media_type, media_url, duration_sec, reps, rest_sec, sort_order, is_active, updated_at) VALUES
((SELECT id FROM categories WHERE slug='chest'), 'Hít đất tiêu chuẩn (Standard Push-up)', 'Hai tay rộng bằng vai, hạ ngực gần sát sàn rồi đẩy thân người lên.', 'VIDEO', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', NULL, 12, 20, 20, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='chest'), 'Hít đất quỳ gối (Knee Push-up)', 'Tương tự hít đất nhưng chống hai gối xuống sàn, phù hợp người mới.', 'GIF', 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600', NULL, 12, 15, 21, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='chest'), 'Hít đất tay rộng (Wide Push-up)', 'Đặt hai bàn tay rộng hơn vai để tập trung vào cơ ngực ngoài.', 'GIF', 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600', NULL, 10, 20, 22, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='chest'), 'Giữ tư thế chống đẩy (Push-up Hold)', 'Giữ nguyên vị trí ở điểm cao nhất của bài hít đất, thở đều.', 'GIF', 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600', 30, NULL, 15, 23, TRUE, NOW());

-- Nạp bộ bài tập hoàn chỉnh cho Nhóm cơ Chân (LEGS)
INSERT INTO exercises (category_id, name, description, media_type, media_url, duration_sec, reps, rest_sec, sort_order, is_active, updated_at) VALUES
((SELECT id FROM categories WHERE slug='legs'), 'Squat gánh thân người (Bodyweight Squat)', 'Đứng rộng bằng vai, hạ hông ra sau như ngồi ghế rồi đứng thẳng.', 'VIDEO', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 15, 20, 30, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='legs'), 'Squat bật nhảy (Jump Squat)', 'Thực hiện động tác squat sau đó bật nhảy lên cao hết mức.', 'VIDEO', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 12, 30, 31, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='legs'), 'Chùng chân bước lùi (Reverse Lunge)', 'Bước một chân ra sau, hạ gối vuông góc 90 độ rồi đẩy về ban đầu.', 'GIF', 'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=600', NULL, 12, 20, 32, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='legs'), 'Sumo Squat (Mở rộng chân)', 'Đứng chân rộng hơn vai, mũi chân hướng ra ngoài 45 độ, hạ hông thấp.', 'GIF', 'https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=600', NULL, 15, 20, 33, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='legs'), 'Nâng gót chân (Calf Raise)', 'Nhón hai gót chân lên cao hết cỡ, siết cơ bắp chân rồi hạ xuống.', 'GIF', 'https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=600', 30, NULL, 15, 34, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='legs'), 'Cầu mông (Glute Bridge)', 'Nằm ngửa, gập gối, nhấn gót chân đẩy hông lên cao siết chặt mông.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 15, 20, 35, TRUE, NOW());

-- Nạp bộ bài tập hoàn chỉnh cho Nhóm cơ Tay & Lưng (ARMS & BACK)
INSERT INTO exercises (category_id, name, description, media_type, media_url, duration_sec, reps, rest_sec, sort_order, is_active, updated_at) VALUES
((SELECT id FROM categories WHERE slug='arms'), 'Hít đất kim cương (Diamond Push-up)', 'Chụm hai ngón trỏ và ngón cái tạo hình kim cương, tập trung tay sau.', 'GIF', 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600', NULL, 10, 25, 40, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='arms'), 'Nhún tay sau trên ghế (Chair Dips)', 'Đặt hai tay lên gờ ghế phía sau, hạ hông xuống vuông góc rồi đẩy lên.', 'GIF', 'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=600', NULL, 12, 20, 41, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='back'), 'Tư thế siêu nhân (Superman)', 'Nằm sấp, nâng đồng thời tay và chân khỏi sàn siết chặt cơ lưng.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 12, 20, 50, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='back'), 'Chó chim (Bird Dog)', 'Quỳ 4 chi, duỗi thẳng tay trái và chân phải song song mặt sàn.', 'GIF', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600', NULL, 12, 15, 51, TRUE, NOW());

-- Nạp bộ bài tập hoàn chỉnh cho Toàn thân (FULL_BODY)
INSERT INTO exercises (category_id, name, description, media_type, media_url, duration_sec, reps, rest_sec, sort_order, is_active, updated_at) VALUES
((SELECT id FROM categories WHERE slug='full_body'), 'Nhảy dang tay chân (Jumping Jacks)', 'Bật nhảy dạng rộng chân đồng thời đưa hai tay qua đầu.', 'GIF', 'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=600', 45, NULL, 15, 60, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='full_body'), 'Leo núi tại chỗ (Mountain Climber)', 'Tư thế chống đẩy, luân phiên co gối về phía ngực nhịp nhàng.', 'GIF', 'https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=600', 45, NULL, 20, 61, TRUE, NOW()),
((SELECT id FROM categories WHERE slug='full_body'), 'Burpees', 'Kết hợp ngồi xổm, chống đẩy và bật nhảy cao.', 'GIF', 'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=600', NULL, 10, 30, 62, TRUE, NOW());
