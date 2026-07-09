-- =============================================================
-- 1. Users
--    Passwords are BCrypt hash of "123456"
-- =============================================================
INSERT INTO `user` (user_id, username, password_hash, phone, role, is_member, `status`)
VALUES
    ('admin',     'Admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000000', 'ADMIN', TRUE,  'ACTIVE'),
    ('alice',     'Alice',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000001', 'USER',  TRUE,  'ACTIVE'),
    ('bob',       'Bob',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000002', 'USER',  FALSE, 'ACTIVE'),
    ('charlie',   'Charlie',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000003', 'USER',  TRUE,  'ACTIVE'),
    ('diana',     'Diana',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000004', 'USER',  FALSE, 'ACTIVE'),
    ('eve',       'Eve',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000005', 'USER',  TRUE,  'ACTIVE');

-- =============================================================
-- 2. Halls
-- =============================================================
INSERT INTO hall (name, `rows`, `cols`, seat_count, description)
VALUES
    ('Standard', 8,  12, 96,  'Standard Hall: 8 rows × 12 cols, 96 seats'),
    ('IMAX',    15, 20, 300, 'IMAX Hall: 15 rows × 20 cols, 300 seats'),
    ('VIP',      4,  5,  20,  'VIP Hall: 4 rows × 5 cols, 20 seats, leather recliners');

-- =============================================================
-- 3. Seat layout (auto-generated per hall)
-- =============================================================

-- Standard hall: 8 rows x 12 cols
INSERT INTO hall_seat (hall_id, row_num, col_num, status)
WITH RECURSIVE rows_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM rows_gen WHERE n < 8
),
cols_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM cols_gen WHERE n < 12
)
SELECT 1, r.n, c.n, 'STANDARD'
FROM rows_gen r, cols_gen c;

-- IMAX hall: 15 rows x 20 cols, centre area set to VIP (rows 6-10, cols 7-14)
INSERT INTO hall_seat (hall_id, row_num, col_num, status)
WITH RECURSIVE rows_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM rows_gen WHERE n < 15
),
cols_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM cols_gen WHERE n < 20
)
SELECT 2, r.n, c.n,
    CASE WHEN r.n BETWEEN 6 AND 10 AND c.n BETWEEN 7 AND 14 THEN 'VIP' ELSE 'STANDARD' END
FROM rows_gen r, cols_gen c;

-- VIP hall: 4 rows x 5 cols, all VIP
INSERT INTO hall_seat (hall_id, row_num, col_num, status)
WITH RECURSIVE rows_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM rows_gen WHERE n < 4
),
cols_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM cols_gen WHERE n < 5
)
SELECT 3, r.n, c.n, 'VIP'
FROM rows_gen r, cols_gen c;

-- =============================================================
-- 4. Movies
-- =============================================================
INSERT INTO movie (title, poster_url, description, duration, rating, genre, release_date, `status`)
VALUES
    ('Interstellar',
     '/uploads/images/movies/3e39a1e904d646bfa0e08f9ab288cecc.jpg',
     'A team of explorers travel through a wormhole in space in an attempt to ensure humanity''s survival.',
     169, 8.6, 'Sci-Fi,Adventure,Drama',
     '2026-04-10', 'ON'),

    ('Inception',
     '/uploads/images/movies/0ec87ff1fc3e4ca2a92a7ee1759eb6f6.jpg',
     'A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea.',
     148, 8.8, 'Sci-Fi,Action',
     '2026-04-10', 'ON'),

    ('Avatar',
     '/uploads/images/movies/403838585b8646d584b8a547dbaca8b7.webp',
     'A paraplegic Marine is dispatched to the moon Pandora on a unique mission but becomes torn between following orders and protecting an alien civilization.',
     162, 7.9, 'Sci-Fi,Adventure',
     '2026-04-01', 'ON'),

    ('Dune',
     '/uploads/images/movies/acbcd891c18b402bafbe2670e3088817.jpg',
     'Feature adaptation of Frank Herbert''s science fiction novel about the son of a noble family entrusted with the protection of the most valuable asset in the galaxy.',
     155, 8.2, 'Sci-Fi,Adventure,Drama',
     '2026-04-15', 'ON'),

    ('Zootopia 2',
     '/uploads/images/movies/b7c9cb1f6a9240e99cf7cb2c43e394b6.jpg',
     'Detectives Judy Hopps and Nick Wilde return to solve a new mystery in the bustling city of Zootopia.',
     110, 8.5, 'Animation,Adventure,Comedy',
     '2026-04-05', 'ON'),

    ('Avengers: Endgame',
     '/uploads/images/movies/fb3508b8dad7415cb395919208dbd83e.jpg',
     'After the devastating events of Infinity War, the Avengers assemble once more to reverse Thanos'' actions and restore balance to the universe.',
     181, 8.4, 'Action,Sci-Fi,Adventure',
     '2026-04-01', 'ON'),

    ('Forrest Gump',
     '/uploads/images/movies/5fd164a581404afaa52276b0d76260e3.jpg',
     'The story of Forrest Gump, a man with a low IQ but a kind heart, who unwittingly influences several historical events.',
     142, 8.8, 'Drama,Romance',
     '2026-04-01', 'ON'),

    ('Harry Potter and the Sorcerer''s Stone',
     '/uploads/images/movies/54b947848ce4469bbcd296cdb16aba70.jpg',
     'An epic fantasy series following the journey of a young wizard, Harry Potter, and his friends as they battle dark forces.',
     152, 8.5, 'Fantasy,Adventure',
     '2026-04-01', 'ON'),

    ('The Matrix',
     '/uploads/images/movies/6f7eed7945cb4ed6ae86914685734064.jpeg',
     'A computer hacker learns about the true nature of reality and his role in the war against the machines that control it.',
     136, 8.7, 'Sci-Fi,Action',
     '2026-05-01', 'ON'),

    ('Spirited Away',
     '/uploads/images/movies/6d78b52351fc492fb0e8a09347439bf7.jpg',
     'During her family''s move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits.',
     125, 8.6, 'Animation,Fantasy,Adventure',
     '2026-05-01', 'ON');

-- =============================================================
-- 5. Showings
--    Covers different time slots and halls
-- =============================================================
INSERT INTO showing (movie_id, hall_id, show_date, show_time, base_price, `status`)
WITH RECURSIVE dates(d) AS (
    SELECT '2026-07-15' UNION ALL SELECT DATE_ADD(d, INTERVAL 1 DAY) FROM dates WHERE d < '2026-07-19'
),
time_slots AS (
    SELECT '10:00' AS t, 1 AS slot_idx
    UNION ALL SELECT '13:00', 2
    UNION ALL SELECT '15:30', 3
    UNION ALL SELECT '18:00', 4
    UNION ALL SELECT '20:30', 5
)
SELECT
    m.id,
    -- Rotate halls based on (movie_id + day_offset + slot)
    ((m.id + DATEDIFF(d.d, '2026-07-15') + ts.slot_idx - 1) % 3) + 1,
    d.d,
    ts.t,
    -- Ticket price: base + movie coefficient + time surcharge
    15.0 + (m.id * 2.5)
        + CASE WHEN ts.slot_idx IN (4, 5) THEN 8.0 ELSE 0 END      -- Peak hours surcharge
        + CASE WHEN ts.slot_idx = 1 THEN -3.0 ELSE 0 END,           -- Early bird discount
    'SCHEDULED'
FROM movie m, dates d, time_slots ts
-- Each movie gets at most 3 time slots per day to avoid too much data
WHERE ts.slot_idx <= 3 + ((m.id + DATEDIFF(d.d, '2026-07-15')) % 3);

-- =============================================================
-- 6. Generate seat booking records for each showing
--    Core data: one record per seat per showing
-- =============================================================
INSERT INTO seat_booking (showing_id, seat_id, `status`, version)
SELECT s.id, hs.id, 'AVAILABLE', 0
FROM showing s
JOIN hall_seat hs ON hs.hall_id = s.hall_id;

-- Mark some seats as BOOKED (simulating sold)
-- Affects 20% of seats so dashboards have data to display
UPDATE seat_booking sb
JOIN showing s ON s.id = sb.showing_id
SET sb.status = 'BOOKED',
    sb.booked_by = FLOOR(1 + RAND() * 5),          -- Random user 1-5
    sb.booked_at = DATE_ADD(s.show_date, INTERVAL -1 DAY),
    sb.order_id = s.id  -- Temp placeholder using showing_id
WHERE sb.id % 5 = 0;   -- Every 5th seat

-- Mark some seats as LOCKED (simulating in-progress locks, 15 min timeout)
UPDATE seat_booking sb
JOIN showing s ON s.id = sb.showing_id
SET sb.status = 'LOCKED',
    sb.locked_by = 2,
    sb.locked_at = DATE_ADD(NOW(), INTERVAL -5 MINUTE)
WHERE s.show_date = '2026-07-18'
  AND s.show_time = '18:00'
  AND sb.status = 'AVAILABLE'
  AND sb.id % 7 = 0;

-- =============================================================
-- 7. Orders + Items + Payments
-- =============================================================

-- 7a. Orders master
INSERT INTO `order` (order_no, user_id, showing_id, seat_count, total_amount, discount_amount, final_amount, `status`, remark, created_at)
VALUES
    ('20260715000001', 2, 1,  2,  96.00,  19.20,  76.80,  'PAID',  NULL,    '2026-07-14 14:30:00'),
    ('20260715000002', 3, 3,  3,  144.00, 0,       144.00, 'PAID',  NULL,    '2026-07-14 15:00:00'),
    ('20260716000003', 4, 12, 1,  65.00,  13.00,  52.00,  'PAID',  NULL,    '2026-07-15 10:15:00'),
    ('20260717000004', 5, 25, 4,  320.00, 0,       320.00, 'PAID',  NULL,    '2026-07-16 20:00:00'),
    ('20260718000005', 2, 35, 2,  88.00,  17.60,  70.40,  'REFUNDED', 'Cancelled due to personal reasons', '2026-07-17 09:00:00');

-- 7b. Update seat_booking to link to real orders
-- Find booked seats without order_id and link to matching orders
UPDATE seat_booking sb
JOIN `order` o ON o.showing_id = sb.showing_id
SET sb.order_id = o.id
WHERE sb.status = 'BOOKED' AND sb.order_id IS NULL
  AND sb.booked_by = o.user_id;

-- Release seats for order 5 (REFUNDED) back to AVAILABLE
UPDATE seat_booking sb
SET sb.status = 'AVAILABLE',
    sb.booked_by = NULL,
    sb.booked_at = NULL,
    sb.order_id = NULL
WHERE sb.order_id = 5;

-- 7c. Order items (seats)
INSERT INTO order_item (order_id, item_type, item_id, item_name, quantity, unit_price, subtotal)
SELECT o.id, 'SEAT', sb.id, CONCAT('Seat ', hs.row_num, '-', hs.col_num), 1,
       (o.total_amount / o.seat_count),
       (o.total_amount / o.seat_count)
FROM `order` o
JOIN seat_booking sb ON sb.order_id = o.id
JOIN hall_seat hs ON hs.id = sb.seat_id
WHERE o.status = 'PAID';

-- 7d. Order items (snacks) - add snacks to some orders
INSERT INTO order_item (order_id, item_type, item_id, item_name, quantity, unit_price, subtotal)
VALUES
    (1, 'SNACK', 1, 'Popcorn',   2, 3.00,  6.00),
    (1, 'SNACK', 2, 'Coca-Cola', 2, 2.00,  4.00),
    (2, 'SNACK', 3, 'Nachos',    3, 4.50,  13.50),
    (4, 'SNACK', 1, 'Popcorn',   4, 3.00,  12.00),
    (4, 'SNACK', 2, 'Coca-Cola', 4, 2.00,  8.00);

-- 7e. Payment records
INSERT INTO payment (order_id, payment_no, amount, payment_method, `status`, paid_at, created_at)
VALUES
    (1, 'PAY202607150001', 76.80,  'WECHAT', 'SUCCESS', '2026-07-14 14:31:00', '2026-07-14 14:30:30'),
    (2, 'PAY202607150002', 144.00, 'ALIPAY', 'SUCCESS', '2026-07-14 15:01:00', '2026-07-14 15:00:30'),
    (3, 'PAY202607160003', 52.00,  'WECHAT', 'SUCCESS', '2026-07-15 10:16:00', '2026-07-15 10:15:30'),
    (4, 'PAY202607170004', 320.00, 'ALIPAY', 'SUCCESS', '2026-07-16 20:01:00', '2026-07-16 20:00:30');

-- =============================================================
-- 8. Snacks
-- =============================================================
INSERT INTO snack (name, price, image_url, stock, `status`)
VALUES
    ('Popcorn',   3.00, '/uploads/images/snacks/f79f538619df48d6870d31b8925c8379.webp', 100, 'ON'),
    ('Coca-Cola', 2.00, '/uploads/images/snacks/325f69d75097414785c3b98691784a96.png',  200, 'ON'),
    ('Nachos',    4.50, '/uploads/images/snacks/cd144a6459ed468eb5da09d63d0caf6c.png',  80,  'ON'),
    ('Sprite',    2.00, '/uploads/images/snacks/0f45e39f0d614c6588e4acf630eab796.jpeg', 150, 'ON'),
    ('Juice',     3.50, '/uploads/images/snacks/d254f67006be42d5adb06ad944348268.jpeg', 120, 'ON');

-- =============================================================
-- 9. Pricing Rules
-- =============================================================
INSERT INTO pricing_rule (rule_name, rule_value, priority, enabled, description,
    condition_member, condition_weekdays, condition_time_start, condition_time_end,
    condition_ticket_min, condition_ticket_max, condition_seat_ratio_min, condition_seat_ratio_max)
VALUES
    ('Tuesday Half Price',       0.50, 1, TRUE,  'Half price every Tuesday',
        NULL, 'TUESDAY', NULL, NULL, NULL, NULL, NULL, NULL),
    ('Weekend 20% Off',          0.80, 2, TRUE,  '20% off on Saturdays and Sundays',
        NULL, 'SATURDAY,SUNDAY', NULL, NULL, NULL, NULL, NULL, NULL),
    ('Member 20% Off',           0.80, 3, TRUE,  'Members enjoy 20% off',
        TRUE, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    ('Low Occupancy 10% Off',    0.90, 4, TRUE,  '10% off when occupancy is below 30%',
        NULL, NULL, NULL, NULL, NULL, NULL, 0.70, NULL),
    ('Peak Hours 15% Surcharge', 1.15, 5, TRUE,  '15% surcharge during peak hours 18:00-22:00',
        NULL, NULL, '18:00:00', '22:00:00', NULL, NULL, NULL, NULL),
    ('High Occupancy 10% Surcharge', 1.10, 6, TRUE,  '10% surcharge when remaining seats are below 30%',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.30);

-- =============================================================
-- 10. Summary output (console)
-- =============================================================
SELECT 'Seed data loaded successfully!' AS message;
SELECT CONCAT('Users: ', COUNT(*)) AS info FROM `user`;
SELECT CONCAT('Movies: ', COUNT(*)) AS info FROM movie;
SELECT CONCAT('Halls: ', COUNT(*), ' (', SUM(seat_count), ' seats total)') AS info FROM hall;
SELECT CONCAT('Seats: ', COUNT(*)) AS info FROM hall_seat;
SELECT CONCAT('Showings: ', COUNT(*)) AS info FROM showing;
SELECT CONCAT('Seat Bookings: ', COUNT(*), ' (Available:', SUM(CASE WHEN `status`='AVAILABLE' THEN 1 ELSE 0 END),
    ', Locked:', SUM(CASE WHEN `status`='LOCKED' THEN 1 ELSE 0 END),
    ', Booked:', SUM(CASE WHEN `status`='BOOKED' THEN 1 ELSE 0 END), ')') AS info FROM seat_booking;
SELECT CONCAT('Orders: ', COUNT(*)) AS info FROM `order`;
SELECT CONCAT('Snacks: ', COUNT(*)) AS info FROM snack;
SELECT CONCAT('Pricing Rules: ', COUNT(*)) AS info FROM pricing_rule;
