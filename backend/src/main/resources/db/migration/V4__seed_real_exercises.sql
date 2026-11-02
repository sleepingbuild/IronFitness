-- V4__seed_real_exercises.sql
-- Thêm các bài tập thật
INSERT INTO exercises (category_id, name, description, media_type, media_url, duration_sec, reps, rest_sec, sort_order, updated_at) VALUES
((SELECT id FROM categories WHERE slug='abs'), 'Gập bụng (Crunch)', 'Nằm ngửa, gập người đẩy vai lên.', 'GIF', 'https://placehold.co/600x400.gif?text=Crunch', NULL, 15, 10, 1, NOW()),
((SELECT id FROM categories WHERE slug='abs'), 'Plank', 'Giữ thân người thẳng tựa trên khuỷu tay.', 'GIF', 'https://placehold.co/600x400.gif?text=Plank', 45, NULL, 15, 2, NOW()),
((SELECT id FROM categories WHERE slug='chest'), 'Hít đất (Push Up)', 'Hạ thấp người bằng tay rồi đẩy lên.', 'GIF', 'https://placehold.co/600x400.gif?text=Push+Up', NULL, 15, 15, 3, NOW()),
((SELECT id FROM categories WHERE slug='legs'), 'Squat', 'Đứng thẳng rồi hạ hông xuống thấp.', 'GIF', 'https://placehold.co/600x400.gif?text=Squat', NULL, 20, 15, 4, NOW()),
((SELECT id FROM categories WHERE slug='full_body'), 'Jumping Jacks', 'Bật nhảy dang rộng tay chân.', 'GIF', 'https://placehold.co/600x400.gif?text=Jumping+Jacks', 30, NULL, 10, 5, NOW());