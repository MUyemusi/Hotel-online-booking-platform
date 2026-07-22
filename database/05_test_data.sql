USE zhisu_hotel;
SET NAMES utf8mb4;

DELETE FROM payment_record WHERE id = 1;
DELETE FROM order_guest WHERE order_id = 1;
DELETE FROM order_daily_price WHERE order_id = 1;
DELETE FROM booking_order WHERE id = 1;
DELETE FROM daily_inventory WHERE inventory_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 6 DAY);
DELETE FROM room WHERE room_no BETWEEN '301' AND '820';

INSERT INTO room (room_no, floor, room_type_id, room_status, remark)
SELECT CONCAT(f.floor_no, LPAD(n.room_seq, 2, '0')) AS room_no,
       f.floor_no,
       CASE
           WHEN f.floor_no = 3 AND n.room_seq <= 5 THEN 1
           WHEN f.floor_no = 3 THEN 2
           WHEN f.floor_no = 4 AND n.room_seq <= 5 THEN 2
           WHEN f.floor_no = 4 THEN 4
           WHEN f.floor_no = 5 AND n.room_seq <= 10 THEN 3
           WHEN f.floor_no = 5 THEN 4
           WHEN f.floor_no = 6 AND n.room_seq <= 10 THEN 5
           WHEN f.floor_no = 6 THEN 6
           WHEN f.floor_no = 7 AND n.room_seq <= 10 THEN 5
           WHEN f.floor_no = 7 THEN 7
           ELSE 7
       END AS room_type_id,
       'AVAILABLE',
       CONCAT(f.floor_no, CONVERT(0xE5B182E5889DE5A78BE58C96E688BFE997B4 USING utf8mb4))
FROM (
    SELECT 3 AS floor_no UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
    SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
) f
CROSS JOIN (
    SELECT 1 AS room_seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
    SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
    SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
    SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) n;

INSERT INTO daily_inventory (room_type_id, inventory_date, total_rooms, locked_rooms, booked_rooms, available_rooms, status)
SELECT rt.id,
       d.inventory_date,
       COUNT(r.id) AS total_rooms,
       0,
       0,
       COUNT(r.id),
       'ACTIVE'
FROM room_type rt
JOIN room r ON r.room_type_id = rt.id AND r.is_deleted = 0
CROSS JOIN (
    SELECT CURDATE() AS inventory_date
    UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY)
    UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 2 DAY)
    UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 3 DAY)
    UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 4 DAY)
    UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 5 DAY)
    UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 6 DAY)
) d
GROUP BY rt.id, d.inventory_date;

INSERT INTO booking_order (id, order_no, user_id, room_type_id, room_type_name, room_count, check_in_date, check_out_date, total_nights, guest_count, total_amount, total_amount_fen, phone, order_status, payment_status, paid_at, remark) VALUES
(1, 'TEST202607150001', 3, 2, CONVERT(0xE6A087E58786E5A4A7E5BA8AE688BF USING utf8mb4), 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 1, 228.00, 22800, '13800000002', 'PENDING_CHECK_IN', 'PAID', NOW(), CONVERT(0xE5889DE5A78BE58C96E6B58BE8AF95E8AEA2E58D95 USING utf8mb4));

INSERT INTO order_guest (order_id, guest_name, guest_phone, id_type, id_number, is_main) VALUES
(1, CONVERT(0xE6B58BE8AF95E4BD8FE5AEA2 USING utf8mb4), '13800000002', 'ID_CARD', '110101199001010011', 1);

INSERT INTO payment_record (id, order_id, payment_no, user_id, payment_method, payment_status, amount, amount_fen, transaction_id, idempotency_key, paid_at) VALUES
(1, 1, 'PAY202607150001', 3, 'MOCK', 'PAID', 228.00, 22800, 'MOCK-INIT-1', 'INIT-PAYMENT-1', NOW());
