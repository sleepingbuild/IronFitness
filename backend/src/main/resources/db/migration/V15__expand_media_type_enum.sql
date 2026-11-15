-- V15: Thêm 'IMAGE' vào ENUM media_type cho bảng exercises
ALTER TABLE exercises MODIFY COLUMN media_type ENUM('GIF','IMAGE','VIDEO') NOT NULL DEFAULT 'GIF';
