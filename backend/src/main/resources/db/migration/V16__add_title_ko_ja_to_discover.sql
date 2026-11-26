-- V16: Bổ sung cột title_ko và title_ja tương thích 100% với TiDB Cloud & MySQL
ALTER TABLE discover_items ADD COLUMN title_ko VARCHAR(200) NULL;
ALTER TABLE discover_items ADD COLUMN title_ja VARCHAR(200) NULL;
