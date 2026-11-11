-- V10: Dọn dẹp các bản ghi trùng lặp trong discover_items và chuẩn hoá dữ liệu

DELETE FROM discover_items WHERE id > 7;

UPDATE discover_items
SET description = 'Kết hợp đạm và xơ hợp lý giúp cơ bắp phục hồi nhanh hơn sau mỗi buổi tập.'
WHERE id = 2;
