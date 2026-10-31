-- V3: DỮ LIỆU THỬ để dựng giao diện (Agent 6) và kiểm thử API. KHÔNG phải nội dung thật.
-- Ảnh minh họa là ảnh giữ chỗ (placeholder) từ dịch vụ ngoài, không có quyền sử dụng riêng.
-- Nội dung thật do Agent 5 cung cấp (docs/seed/exercises.csv); migration V4 sẽ ẩn các bài này
-- (is_active = FALSE) rồi thêm bài thật. KHÔNG sửa file này sau khi đã chạy.
-- Mỗi bài đúng một trong duration_sec hoặc reps.

INSERT INTO exercises
    (category_id, name, description, media_url, media_type, duration_sec, reps, rest_sec, sort_order, is_active, updated_at)
VALUES
    -- Bụng
    ((SELECT id FROM categories WHERE slug = 'abs'), 'Plank',
     'Chống khuỷu tay và mũi chân xuống sàn, giữ thân người thẳng từ đầu đến gót, siết bụng và không võng lưng.',
     'https://placehold.co/600x400.gif?text=Plank', 'GIF', 30, NULL, 15, 1, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'abs'), 'Gập bụng',
     'Nằm ngửa, co gối, hai tay đặt nhẹ sau đầu. Nâng vai khỏi sàn bằng lực của bụng rồi hạ xuống từ từ.',
     'https://placehold.co/600x400.gif?text=Crunch', 'GIF', NULL, 15, 15, 2, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'abs'), 'Nâng chân nằm',
     'Nằm ngửa, hai chân duỗi thẳng. Nâng hai chân lên vuông góc với sàn rồi hạ xuống chậm, không chạm sàn.',
     'https://placehold.co/600x400.gif?text=Leg+raise', 'GIF', NULL, 12, 20, 3, TRUE, UTC_TIMESTAMP()),

    -- Ngực
    ((SELECT id FROM categories WHERE slug = 'chest'), 'Hít đất',
     'Hai tay chống rộng bằng vai, thân người thẳng. Hạ ngực gần sát sàn rồi đẩy người lên.',
     'https://placehold.co/600x400.gif?text=Push-up', 'GIF', NULL, 10, 20, 1, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'chest'), 'Hít đất quỳ',
     'Giống hít đất nhưng hai gối chạm sàn, phù hợp người mới bắt đầu. Giữ lưng thẳng từ đầu đến gối.',
     'https://placehold.co/600x400.gif?text=Knee+push-up', 'GIF', NULL, 12, 20, 2, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'chest'), 'Giữ tư thế hít đất',
     'Giữ nguyên tư thế chống đẩy với hai tay thẳng, siết bụng và mông, thở đều.',
     'https://placehold.co/600x400.gif?text=Push-up+hold', 'GIF', 20, NULL, 15, 3, TRUE, UTC_TIMESTAMP()),

    -- Chân
    ((SELECT id FROM categories WHERE slug = 'legs'), 'Squat',
     'Đứng rộng bằng vai, hạ hông ra sau như ngồi xuống ghế, đầu gối cùng hướng mũi chân, rồi đứng thẳng lên.',
     'https://placehold.co/600x400.gif?text=Squat', 'GIF', NULL, 15, 20, 1, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'legs'), 'Lunge',
     'Bước một chân ra trước, hạ thấp cho đến khi hai đầu gối tạo góc khoảng 90 độ, rồi đẩy về vị trí ban đầu.',
     'https://placehold.co/600x400.gif?text=Lunge', 'GIF', NULL, 10, 20, 2, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'legs'), 'Nâng gót chân',
     'Đứng thẳng, nhón gót lên cao hết mức rồi hạ xuống chậm. Có thể vịn tường để giữ thăng bằng.',
     'https://placehold.co/600x400.gif?text=Calf+raise', 'GIF', 30, NULL, 15, 3, TRUE, UTC_TIMESTAMP()),

    -- Toàn thân
    ((SELECT id FROM categories WHERE slug = 'full_body'), 'Nhảy dang tay chân',
     'Nhảy hai chân sang hai bên, đồng thời đưa hai tay lên cao, rồi nhảy về tư thế đứng thẳng.',
     'https://placehold.co/600x400.gif?text=Jumping+jack', 'GIF', 30, NULL, 15, 1, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'full_body'), 'Leo núi',
     'Ở tư thế chống đẩy, lần lượt đưa mỗi đầu gối về phía ngực với nhịp nhanh, giữ hông thấp.',
     'https://placehold.co/600x400.gif?text=Mountain+climber', 'GIF', 30, NULL, 20, 2, TRUE, UTC_TIMESTAMP()),
    ((SELECT id FROM categories WHERE slug = 'full_body'), 'Burpee',
     'Từ tư thế đứng, ngồi xuống chống tay, đá chân ra sau thành tư thế hít đất, thu chân về rồi bật nhảy lên.',
     'https://placehold.co/600x400.gif?text=Burpee', 'GIF', NULL, 8, 30, 3, TRUE, UTC_TIMESTAMP());
