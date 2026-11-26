-- V16: Bổ sung cột title_ko và title_ja cho bảng discover_items
ALTER TABLE discover_items
ADD COLUMN title_ko VARCHAR(200) NULL AFTER title_en,
ADD COLUMN title_ja VARCHAR(200) NULL AFTER title_ko;
