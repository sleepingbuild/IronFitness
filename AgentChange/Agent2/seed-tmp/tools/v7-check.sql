-- v7-check.sql
-- Chay SAU khi V7 da ap dung len database co du lieu cu (v7-insert-legacy-data.sql).
SET NOCOUNT ON;

PRINT '--- 1. Du lieu cu da sang cot moi (mong doi: 0001 co member_package_id, 0002 co pos_order_id)';
SELECT transaction_code,
       CASE WHEN member_package_id IS NOT NULL THEN 'co' ELSE '-' END AS member_package_id,
       CASE WHEN pos_order_id IS NOT NULL THEN 'co' ELSE '-' END AS pos_order_id,
       amount, status
FROM payment_transactions WHERE transaction_code LIKE 'TX-V7-LEGACY-%' ORDER BY transaction_code;

PRINT '--- 2. Tro dung ban ghi cha (mong doi: OK, OK)';
SELECT pt.transaction_code,
       CASE WHEN mp.id IS NOT NULL THEN 'OK' ELSE 'SAI' END AS tro_toi_member_package
FROM payment_transactions pt LEFT JOIN member_packages mp ON mp.id = pt.member_package_id
WHERE pt.transaction_code = 'TX-V7-LEGACY-0001';
SELECT pt.transaction_code,
       CASE WHEN po.id IS NOT NULL THEN 'OK' ELSE 'SAI' END AS tro_toi_pos_order
FROM payment_transactions pt LEFT JOIN pos_orders po ON po.id = pt.pos_order_id
WHERE pt.transaction_code = 'TX-V7-LEGACY-0002';

PRINT '--- 3. Rang buoc "dung mot trong hai" (mong doi: bi chan)';
BEGIN TRY
    INSERT INTO payment_transactions (user_id, amount, payment_method, status, transaction_code)
    VALUES (NULL, 1, 'CASH', 'PENDING', 'TX-V7-BAD');
    PRINT 'SAI: chen duoc dong khong thuoc loai nao';
    DELETE FROM payment_transactions WHERE transaction_code = 'TX-V7-BAD';
END TRY
BEGIN CATCH
    PRINT 'OK: bi chan - ' + ERROR_MESSAGE();
END CATCH;

PRINT '--- 4. revenueByDay sau V7 (mong doi: 2 dong, MEMBERSHIP 500000 va POS 45000)';
SELECT CAST(COALESCE(updated_at, created_at) AS DATE) AS day_value,
       CASE WHEN member_package_id IS NOT NULL THEN 'MEMBERSHIP' ELSE 'POS' END AS related_type,
       SUM(amount) AS total
FROM payment_transactions
WHERE status = 'SUCCESS'
  AND COALESCE(updated_at, created_at) >= '2000-01-01'
  AND COALESCE(updated_at, created_at) < '2100-01-01'
GROUP BY CAST(COALESCE(updated_at, created_at) AS DATE),
         CASE WHEN member_package_id IS NOT NULL THEN 'MEMBERSHIP' ELSE 'POS' END;

PRINT '--- 5. topPackages sau V7 (mong doi: Goi V7, 1 luot, 500000; khong tinh don POS)';
SELECT TOP (10)
       p.id AS package_id,
       p.name AS package_name,
       COUNT(*) AS registrations,
       SUM(pt.amount) AS revenue
FROM payment_transactions pt
JOIN member_packages mp ON mp.id = pt.member_package_id
JOIN packages p ON p.id = mp.package_id
WHERE pt.status = 'SUCCESS'
  AND COALESCE(pt.updated_at, pt.created_at) >= '2000-01-01'
  AND COALESCE(pt.updated_at, pt.created_at) < '2100-01-01'
GROUP BY p.id, p.name
ORDER BY COUNT(*) DESC, SUM(pt.amount) DESC, p.name ASC;
