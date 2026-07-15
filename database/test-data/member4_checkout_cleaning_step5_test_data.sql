USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4 step 5 acceptance data. Re-runnable and scoped to M4S5 orders only.
-- No synthetic rooms are created; all scenarios use official room numbers.

DELETE cqt FROM checkin_qr_token cqt
JOIN booking_order bo ON bo.id = cqt.order_id
WHERE bo.order_no LIKE 'M4S5-%';

DELETE rsl FROM room_status_log rsl
JOIN booking_order bo ON bo.id = rsl.related_order_id
WHERE bo.order_no LIKE 'M4S5-%';

DELETE ra FROM room_assignment ra
JOIN booking_order bo ON bo.id = ra.order_id
WHERE bo.order_no LIKE 'M4S5-%';

DELETE og FROM order_guest og
JOIN booking_order bo ON bo.id = og.order_id
WHERE bo.order_no LIKE 'M4S5-%';

DELETE odp FROM order_daily_price odp
JOIN booking_order bo ON bo.id = odp.order_id
WHERE bo.order_no LIKE 'M4S5-%';

DELETE FROM booking_order WHERE order_no LIKE 'M4S5-%';

UPDATE room
SET room_status = 'AVAILABLE',
    maintenance_reason = NULL,
    remark = 'M4S5 reset official room',
    version = 0,
    updated_at = NOW()
WHERE room_no IN ('311','312','313','314','315','316','317','318','319','320')
  AND is_deleted = 0;

SET @m4s5_room_type_id = (
    SELECT room_type_id FROM room WHERE room_no = '311' AND is_deleted = 0 LIMIT 1
);
SET @m4s5_room_type_name = (
    SELECT name FROM room_type WHERE id = @m4s5_room_type_id LIMIT 1
);

INSERT INTO booking_order (
    order_no, user_id, room_type_id, room_type_name, room_count,
    check_in_date, check_out_date, total_nights, guest_count,
    total_amount, total_amount_fen, phone, order_status, payment_status, paid_at, remark
) VALUES
('M4S5-CHECKOUT-SINGLE', 3, @m4s5_room_type_id, @m4s5_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900005001', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S5 checkout single'),
('M4S5-CHECKOUT-MULTI', 3, @m4s5_room_type_id, @m4s5_room_type_name, 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 2, 912.00, 91200, '13900005002', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S5 checkout multi'),
('M4S5-IDEMPOTENT', 3, @m4s5_room_type_id, @m4s5_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900005003', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S5 checkout idempotent'),
('M4S5-CONCURRENT', 3, @m4s5_room_type_id, @m4s5_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900005004', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S5 checkout concurrent'),
('M4S5-ROLLBACK', 3, @m4s5_room_type_id, @m4s5_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900005005', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S5 checkout rollback'),
('M4S5-NOT-CHECKED-IN', 3, @m4s5_room_type_id, @m4s5_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900005006', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S5 not checked in'),
('M4S5-CANCELLED', 3, @m4s5_room_type_id, @m4s5_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900005007', 'CANCELLED', 'PAID', NOW(), 'M4S5 cancelled checkout');

INSERT INTO order_guest (order_id, guest_name, guest_phone, id_type, id_number, is_main)
SELECT id, CONCAT('M4S5 Guest ', order_no), phone, 'ID_CARD', CONCAT('M4S5-ID-', id), 1
FROM booking_order
WHERE order_no LIKE 'M4S5-%';
