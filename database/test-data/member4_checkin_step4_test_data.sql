USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4 step 4 acceptance data. Re-runnable and scoped to M4S4 only.

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

INSERT INTO room (room_no, floor, room_type_id, room_status, maintenance_reason, remark, is_deleted, version) VALUES
('M4S4-301', 3, 2, 'AVAILABLE', NULL, 'M4S4 step4 single room', 0, 0),
('M4S4-302', 3, 2, 'AVAILABLE', NULL, 'M4S4 step4 multi room A', 0, 0),
('M4S4-303', 3, 2, 'AVAILABLE', NULL, 'M4S4 step4 multi room B', 0, 0),
('M4S4-304', 3, 2, 'AVAILABLE', NULL, 'M4S4 step4 concurrent room', 0, 0),
('M4S4-310', 3, 2, 'AVAILABLE', NULL, 'M4S4 step4 rollback room', 0, 0),
('M4S4-401', 4, 3, 'AVAILABLE', NULL, 'M4S4 step4 type mismatch room', 0, 0),
('M4S4-305', 3, 2, 'OCCUPIED', NULL, 'M4S4 step4 occupied room', 0, 0),
('M4S4-306', 3, 2, 'DIRTY', NULL, 'M4S4 step4 dirty room', 0, 0),
('M4S4-307', 3, 2, 'MAINTENANCE', 'M4S4 test maintenance', 'M4S4 step4 maintenance room', 0, 0),
('M4S4-308', 3, 2, 'DISABLED', NULL, 'M4S4 step4 disabled room', 0, 0),
('M4S4-309', 3, 2, 'RESERVED', NULL, 'M4S4 step4 reserved room', 0, 0);

INSERT INTO booking_order (
    order_no, user_id, room_type_id, room_type_name, room_count,
    check_in_date, check_out_date, total_nights, guest_count,
    total_amount, total_amount_fen, phone, order_status, payment_status, paid_at, remark
) VALUES
('M4S4-CHECKIN-SINGLE', 3, 2, '标准大床房', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004001', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 single check-in'),
('M4S4-CHECKIN-MULTI', 3, 2, '标准大床房', 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 2, 912.00, 91200, '13900004002', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 multi check-in'),
('M4S4-UNPAID', 3, 2, '标准大床房', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004003', 'PENDING_PAYMENT', 'UNPAID', NULL, 'M4S4 unpaid'),
('M4S4-TYPE-MISMATCH', 3, 2, '标准大床房', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004004', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 type mismatch'),
('M4S4-CONCURRENT-A', 3, 2, '标准大床房', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004005', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 concurrent A'),
('M4S4-CONCURRENT-B', 3, 2, '标准大床房', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004006', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 concurrent B'),
('M4S4-ROLLBACK', 3, 2, '标准大床房', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 1, 456.00, 45600, '13900004007', 'PENDING_CHECK_IN', 'PAID', NOW(), 'M4S4 rollback');

INSERT INTO order_guest (order_id, guest_name, guest_phone, id_type, id_number, is_main)
SELECT id, CONCAT('M4S4 Guest ', order_no), phone, 'ID_CARD', CONCAT('M4S4-ID-', id), 1
FROM booking_order
WHERE order_no LIKE 'M4S4-%';
