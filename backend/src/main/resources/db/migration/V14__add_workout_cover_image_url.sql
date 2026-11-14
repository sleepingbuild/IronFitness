-- V14: Bổ sung cột cover_image_url cho bảng workouts
ALTER TABLE workouts ADD COLUMN cover_image_url VARCHAR(500) NULL AFTER equipment;

UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800' WHERE primary_muscle = 'Bụng' AND (cover_image_url IS NULL OR cover_image_url = '');
UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800' WHERE primary_muscle = 'Ngực' AND (cover_image_url IS NULL OR cover_image_url = '');
UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1434596922112-19c563067271?w=800' WHERE primary_muscle = 'Chân' AND (cover_image_url IS NULL OR cover_image_url = '');
UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=800' WHERE primary_muscle = 'Lưng' AND (cover_image_url IS NULL OR cover_image_url = '');
UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800' WHERE primary_muscle = 'Toàn thân' AND (cover_image_url IS NULL OR cover_image_url = '');
UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=800' WHERE type = 'tabata' AND (cover_image_url IS NULL OR cover_image_url = '');
UPDATE workouts SET cover_image_url = 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800' WHERE type = 'stretch' AND (cover_image_url IS NULL OR cover_image_url = '');
