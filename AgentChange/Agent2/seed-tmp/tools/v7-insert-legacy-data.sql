-- v7-insert-legacy-data.sql
-- Chen 2 giao dich o DINH DANG CU (truoc V7): 1 MEMBERSHIP va 1 POS, kem du lieu cha de khoa ngoai hop le.
-- Chay tren database da migrate den V9 NHUNG CHUA co V7. Chi dung tren database thu rieng.
SET NOCOUNT ON;

DECLARE @member UNIQUEIDENTIFIER = NEWID(), @staff UNIQUEIDENTIFIER = NEWID(), @pkg UNIQUEIDENTIFIER = NEWID(),
        @mp UNIQUEIDENTIFIER = NEWID(), @branch UNIQUEIDENTIFIER = NEWID(), @pos UNIQUEIDENTIFIER = NEWID();

INSERT INTO users (id, full_name, email, password_hash) VALUES
    (@member, N'Hoi vien V7', 'v7-member@example.com', 'x'),
    (@staff,  N'Nhan vien V7', 'v7-staff@example.com', 'x');
INSERT INTO branches (id, name) VALUES (@branch, N'Chi nhanh V7');
INSERT INTO packages (id, name, price, package_type) VALUES (@pkg, N'Goi V7', 500000, 'TIME_BASED');
INSERT INTO member_packages (id, user_id, package_id, branch_id, start_date)
    VALUES (@mp, @member, @pkg, @branch, CAST(SYSUTCDATETIME() AS DATE));
INSERT INTO pos_orders (id, member_id, staff_id, branch_id, payment_method)
    VALUES (@pos, @member, @staff, @branch, 'CASH');

INSERT INTO payment_transactions (user_id, related_type, related_id, amount, payment_method, status, transaction_code) VALUES
    (@member, 'MEMBERSHIP', @mp,  500000, 'CASH', 'SUCCESS', 'TX-V7-LEGACY-0001'),
    (@staff,  'POS',        @pos,  45000, 'CASH', 'SUCCESS', 'TX-V7-LEGACY-0002');

PRINT 'Da chen 2 giao dich cu (TX-V7-LEGACY-0001 MEMBERSHIP, TX-V7-LEGACY-0002 POS)';
