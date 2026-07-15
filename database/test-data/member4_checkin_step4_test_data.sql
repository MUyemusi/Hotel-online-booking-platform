USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4 step 4 acceptance data. Re-runnable and scoped to M4S4 orders only.
-- Test rooms use official room numbers from the 120-room inventory.

DELETE cqt FROM checkin_qr_token cqt
JOIN booking_order bo ON bo.id = cqt.order_id
WHERE bo.order_no LIKE 'M4S4-%';

DELETE rsl FROM room_status_log rsl
JOIN booking_order bo ON bo.id = rsl.related_order_id
WHERE bo.order_no LIKE 'M4S4-%';

DELETE ra FROM room_assignment ra
JOIN booking_order bo ON bo.id = ra.order_id
WHERE bo.order_no LIKE 'M4S4-%';

DELETE og FROM order_guest og
JOIN booking_order bo ON bo.id = og.order_id
WHERE bo.order_no LIKE 'M4S4-%';

DELETE odp FROM order_daily_price odp
JOIN booking_order bo ON bo.id = odp.order_id
WHERE bo.order_no LIKE 'M4S4-%';

DELETE FROM booking_order WHERE order_no LIKE 'M4S4-%';
DELETE FROM room WHERE room_no LIKE 'M4S4-%';

UPDATE room
SET room_status = 'AVAILABLE',
    maintenance_reason = NULL,
    remark = 'M4S4 reset official room',
    version = 0,
    updated_at = NOW()
WHERE room_no IN ('301','302','303','304','305','306','307','308','309','310','401')
  AND is_deleted = 0;

SET @m4s4_room_type_id = (
    SELECT room_type_id FROM room WHERE room_no = '301' AND is_deleted = 0 LIMIT 1
);
SET @m4s4_room_type_name = (
    SELECT name FROM room_type WHERE id = @m4s4_room_type_id LIMIT 1
);
SET @m4s4_mismatch_room_type_id = (
    SELECT room_type_id FROM room WHERE room_no = '401' AND is_deleted = 0 LIMIT 1
);

INSERT INTO booking_order (
    order_no, user_id, room_type_id, room_type_name, room_count,
    check_in_date, check_out_date, total_nights, guest_count,
    total_amount, total_amount_fen, phone, order_status, payment_status, paid_at, remark
) VALUES
('M4S4-CHECKIN-SINGLE', 3, @m4s4_room_type_id, @m4s4_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004001', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 single check-in'),
('M4S4-CHECKIN-MULTI', 3, @m4s4_room_type_id, @m4s4_room_type_name, 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 2, 912.00, 91200, '13900004002', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 multi check-in'),
('M4S4-UNPAID', 3, @m4s4_room_type_id, @m4s4_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004003', 'PENDING_PAYMENT', 'UNPAID', NULL, 'M4S4 unpaid'),
('M4S4-TYPE-MISMATCH', 3, @m4s4_room_type_id, @m4s4_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004004', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 type mismatch'),
('M4S4-CONCURRENT-A', 3, @m4s4_room_type_id, @m4s4_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004005', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 concurrent A'),
('M4S4-CONCURRENT-B', 3, @m4s4_room_type_id, @m4s4_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004006', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 concurrent B'),
('M4S4-ROLLBACK', 3, @m4s4_room_type_id, @m4s4_room_type_name, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004007', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 rollback');

INSERT INTO order_guest (order_id, guest_name, guest_phone, id_type, id_number, is_main)
SELECT id, CONCAT('M4S4 Guest ', order_no), phone, 'ID_CARD', CONCAT('M4S4-ID-', id), 1
FROM booking_order
WHERE order_no LIKE 'M4S4-%';
