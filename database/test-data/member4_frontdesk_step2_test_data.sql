USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4 step 2 frontdesk order query and check-in verification data.
-- Idempotent cleanup only touches the dedicated M4S2 order number prefix.

SET @today = CURDATE();
SET @tomorrow = DATE_ADD(@today, INTERVAL 1 DAY);
SET @future = DATE_ADD(@today, INTERVAL 2 DAY);
SET @future_checkout = DATE_ADD(@today, INTERVAL 3 DAY);

SET @room_type_id = 2;
SET @room_type_name = (SELECT name FROM room_type WHERE id = @room_type_id);
SET @available_count = (
    SELECT COUNT(*)
    FROM room
    WHERE room_type_id = @room_type_id
      AND room_status = 'AVAILABLE'
      AND is_deleted = 0
);

DELETE odp
FROM order_daily_price odp
JOIN booking_order bo ON bo.id = odp.order_id
WHERE bo.order_no LIKE 'M4S2-%';

DELETE og
FROM order_guest og
JOIN booking_order bo ON bo.id = og.order_id
WHERE bo.order_no LIKE 'M4S2-%';

DELETE FROM booking_order WHERE order_no LIKE 'M4S2-%';

INSERT INTO booking_order (
    order_no, user_id, room_type_id, room_type_name, room_count,
    check_in_date, check_out_date, total_nights, guest_count,
    total_amount, total_amount_fen, phone, order_status, payment_status,
    paid_at, remark
) VALUES
('M4S2-CHECKIN-OK', 3, @room_type_id, @room_type_name, 1,
 @today, @tomorrow, 1, 1, 228.00, 22800, '13890000001',
 'PENDING_CHECK_IN', 'PAID', NOW(), 'member4 step2 can check in order'),
('M4S2-UNPAID', 3, @room_type_id, @room_type_name, 1,
 @today, @tomorrow, 1, 1, 228.00, 22800, '13890000002',
 'PENDING_CHECK_IN', 'UNPAID', NULL, 'member4 step2 unpaid order'),
('M4S2-FUTURE', 3, @room_type_id, @room_type_name, 1,
 @future, @future_checkout, 1, 1, 228.00, 22800, '13890000003',
 'PENDING_CHECK_IN', 'PAID', NOW(), 'member4 step2 future check-in order'),
('M4S2-NOT-ENOUGH', 3, @room_type_id, @room_type_name, @available_count + 1,
 @today, @tomorrow, 1, @available_count + 1, 228.00 * (@available_count + 1), 22800 * (@available_count + 1), '13890000004',
 'PENDING_CHECK_IN', 'PAID', NOW(), 'member4 step2 insufficient room order');

INSERT INTO order_guest (order_id, guest_name, guest_phone, id_type, id_number, is_main)
SELECT id, guest_name, guest_phone, 'ID_CARD', id_number, 1
FROM (
    SELECT 'M4S2-CHECKIN-OK' AS order_no, 'M4S2 Test Guest A' AS guest_name, '13890000001' AS guest_phone, '110101199001010001' AS id_number
    UNION ALL SELECT 'M4S2-UNPAID', 'M4S2 Test Guest B', '13890000002', '110101199001010002'
    UNION ALL SELECT 'M4S2-FUTURE', 'M4S2 Test Guest C', '13890000003', '110101199001010003'
    UNION ALL SELECT 'M4S2-NOT-ENOUGH', 'M4S2 Test Guest D', '13890000004', '110101199001010004'
) seed
JOIN booking_order bo ON bo.order_no = seed.order_no;

INSERT INTO order_daily_price (order_id, price_date, daily_price)
SELECT id, check_in_date, 228.00
FROM booking_order
WHERE order_no LIKE 'M4S2-%'
ON DUPLICATE KEY UPDATE daily_price = VALUES(daily_price);

SELECT id, order_no, room_type_id, room_count, check_in_date, check_out_date,
       order_status, payment_status, phone
FROM booking_order
WHERE order_no LIKE 'M4S2-%'
ORDER BY order_no;
