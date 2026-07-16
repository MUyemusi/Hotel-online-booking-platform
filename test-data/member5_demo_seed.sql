USE zhisu_hotel;

-- Member5 demo content data
DELETE FROM hotel_banner;
INSERT INTO hotel_banner (
    id, title, image_url, link_url, sort_no, status, created_at, updated_at
) VALUES
(1, 'Summer Promotion', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1600&q=80', '/promotions/summer', 1, 'PUBLISHED', '2026-07-15 10:00:00', '2026-07-15 10:00:00'),
(2, 'Business Travel Pick', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1600&q=80', '/rooms/business', 2, 'PUBLISHED', '2026-07-15 10:05:00', '2026-07-15 10:05:00'),
(3, 'Family Weekend Plan', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1600&q=80', '/family/weekend', 3, 'OFFLINE', '2026-07-15 10:10:00', '2026-07-15 10:10:00');

DELETE FROM announcement;
INSERT INTO announcement (
    id, title, content, status, is_top, publish_time, offline_time, created_by, created_at, updated_at
) VALUES
(1, 'Summer Check-in Reminder', 'Please bring a valid ID card when checking in during the busy summer season.', 'PUBLISHED', 1, '2026-07-15 09:00:00', NULL, 1, '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
(2, 'Parking Maintenance Notice', 'The ground parking area will be partially maintained this Saturday morning.', 'PUBLISHED', 0, '2026-07-15 09:10:00', NULL, 1, '2026-07-15 09:10:00', '2026-07-15 09:10:00'),
(3, 'Mid-Autumn Activity Preview', 'Holiday snacks and family packages will be announced soon.', 'OFFLINE', 0, '2026-07-15 09:20:00', '2026-07-15 12:00:00', 1, '2026-07-15 09:20:00', '2026-07-15 12:00:00');

-- Inventory warning demo case
UPDATE daily_inventory
SET available_rooms = 1,
    locked_rooms = 2,
    booked_rooms = 7,
    updated_at = CURRENT_TIMESTAMP
WHERE room_type_id = 5
  AND inventory_date IN ('2026-07-18', '2026-07-19');

-- Manual daily price override demo case
UPDATE daily_price
SET manual_price = 599.00,
    manual_override = 1,
    override_reason = 'expo manual price demo',
    final_price = 599.00,
    updated_at = CURRENT_TIMESTAMP
WHERE room_type_id = 5
  AND business_date = '2026-07-18';

