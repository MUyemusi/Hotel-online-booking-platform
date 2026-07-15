USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4 step 3 QR test data is intentionally small and idempotent.
-- It reuses M4S2-CHECKIN-OK and only removes member4 QR tokens for those orders.

INSERT INTO system_config (config_key, config_value, config_name, value_type, config_group, description, status)
VALUES ('checkin_qr_expire_minutes', '10', 'Check-in QR expire minutes', 'INTEGER', 'FRONTDESK', 'Member 4 check-in QR token validity', 'ENABLED')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), status = VALUES(status);

DELETE cqt
FROM checkin_qr_token cqt
JOIN booking_order bo ON bo.id = cqt.order_id
WHERE bo.order_no LIKE 'M4S2-%';

UPDATE booking_order
SET check_in_date = CURDATE(),
    check_out_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY),
    order_status = 'PENDING_CHECK_IN',
    payment_status = 'PAID'
WHERE order_no = 'M4S2-CHECKIN-OK';

UPDATE booking_order
SET check_in_date = CURDATE(),
    check_out_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY),
    order_status = 'PENDING_CHECK_IN',
    payment_status = 'UNPAID'
WHERE order_no = 'M4S2-UNPAID';

UPDATE booking_order
SET check_in_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY),
    check_out_date = DATE_ADD(CURDATE(), INTERVAL 4 DAY),
    order_status = 'PENDING_CHECK_IN',
    payment_status = 'PAID'
WHERE order_no = 'M4S2-FUTURE';

SELECT id, order_no, order_status, payment_status, check_in_date, check_out_date
FROM booking_order
WHERE order_no LIKE 'M4S2-%'
ORDER BY order_no;
