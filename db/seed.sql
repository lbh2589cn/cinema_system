-- =============================================================
-- 电影院购票系统 - 初始化种子数据
-- 说明: 用于开发/演示环境初始化
-- 注意: 生产环境请勿自动执行此脚本
-- =============================================================

-- =============================================================
-- 1. 默认用户
--    密码均为 "123456" 的 BCrypt 哈希
-- =============================================================
INSERT INTO "user" (user_id, username, password_hash, phone, nickname, role, is_member, status)
VALUES
    ('admin',     'Admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000000', '管理员', 'ADMIN', TRUE, 'ACTIVE'),
    ('alice',     'Alice',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000001', '爱丽丝', 'USER',  TRUE, 'ACTIVE'),
    ('bob',       'Bob',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000002', '鲍勃',   'USER',  FALSE, 'ACTIVE');

-- =============================================================
-- 2. 影厅（3个）
-- =============================================================
INSERT INTO hall (name, rows, cols, seat_count, description)
VALUES
    ('Standard', 5,  10, 50,  '标准厅：5行 × 10列，共50座'),
    ('IMAX',    15, 20, 300, 'IMAX巨幕厅：15行 × 20列，共300座'),
    ('VIP',      3,  4,  12,  'VIP厅：3行 × 4列，共12座，真皮沙发');

-- =============================================================
-- 3. 座位布局（根据影厅自动生成）
-- =============================================================

-- Standard 厅：5行×10列 (行号 1-5, 列号 1-10)
INSERT INTO hall_seat (hall_id, row_num, col_num, seat_type)
SELECT 1, r, c, 'STANDARD'
FROM generate_series(1, 5) r, generate_series(1, 10) c;

-- IMAX 厅：15行×20列 (行号 1-15, 列号 1-20)
INSERT INTO hall_seat (hall_id, row_num, col_num, seat_type)
SELECT 2, r, c, 'STANDARD'
FROM generate_series(1, 15) r, generate_series(1, 20) c;

-- VIP 厅：3行×4列 (行号 1-3, 列号 1-4, 全部 VIP)
INSERT INTO hall_seat (hall_id, row_num, col_num, seat_type)
SELECT 3, r, c, 'VIP'
FROM generate_series(1, 3) r, generate_series(1, 4) c;

-- =============================================================
-- 4. 电影（8部，与原系统保持一致）
-- =============================================================
INSERT INTO movie (title, poster_url, description, duration, rating, genre, release_date, status)
VALUES
    ('Interstellar',
     'assets/posters/interstellar.jpg',
     'A team of explorers travel through a wormhole in space in an attempt to ensure humanity''s survival.',
     169, 8.6, 'Sci-Fi,Adventure,Drama',
     '2026-04-10', 'ON'),

    ('Inception',
     'assets/posters/inception.jpg',
     'A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea.',
     148, 8.8, 'Sci-Fi,Action',
     '2026-04-10', 'ON'),

    ('Avatar',
     'assets/posters/avatar.jpg',
     'A paraplegic Marine is dispatched to the moon Pandora on a unique mission but becomes torn between following orders and protecting an alien civilization.',
     162, 7.9, 'Sci-Fi,Adventure',
     '2026-04-01', 'ON'),

    ('Dune',
     'assets/posters/dune.jpg',
     'Feature adaptation of Frank Herbert''s science fiction novel about the son of a noble family entrusted with the protection of the most valuable asset in the galaxy.',
     155, 8.2, 'Sci-Fi,Adventure,Drama',
     '2026-04-15', 'ON'),

    ('Zootopia',
     'assets/posters/zootopia.jpg',
     'Detectives Judy Hopps and Nick Wilde return to solve a new mystery in the bustling city of Zootopia.',
     110, 8.5, 'Animation,Adventure,Comedy',
     '2026-04-05', 'ON'),

    ('Avengers: Endgame',
     'assets/posters/avengers.jpg',
     'After the devastating events of Infinity War, the Avengers assemble once more to reverse Thanos'' actions and restore balance to the universe.',
     181, 8.4, 'Action,Sci-Fi,Adventure',
     '2026-04-01', 'ON'),

    ('Forrest Gump',
     'assets/posters/forrest_gump.jpg',
     'The story of Forrest Gump, a man with a low IQ but a kind heart, who unwittingly influences several historical events in the United States while pursuing his lifelong love, Jenny.',
     142, 8.8, 'Drama,Romance',
     '2026-04-01', 'ON'),

    ('Harry Potter',
     'assets/posters/harry_potter.jpg',
     'An epic fantasy series following the journey of a young wizard, Harry Potter, and his friends as they battle dark forces and uncover the secrets of the magical world.',
     152, 8.5, 'Fantasy,Adventure',
     '2026-04-01', 'ON');

-- =============================================================
-- 5. 默认排片（为每部电影生成一场示范排片）
-- =============================================================
INSERT INTO showing (movie_id, hall_id, show_date, show_time, base_price, status)
SELECT
    m.id,
    ((m.id - 1) % 3) + 1,  -- 轮流分配到 3 个影厅
    '2026-07-15'::DATE + (m.id % 5) * INTERVAL '1 day',  -- 不同日期
    (14 + (m.id % 4))::TEXT || ':00'::TIME,  -- 14:00 ~ 17:00
    10.0 + (m.id * 3),                        -- 价格递增 13~34
    'SCHEDULED'
FROM movie m;

-- =============================================================
-- 6. 零食（3款）
-- =============================================================
INSERT INTO snack (name, price, image_url, stock, status)
VALUES
    ('Popcorn',   3.00, 'assets/snacks/popcorn.png', 100, 'ON'),
    ('Coca-Cola', 2.00, 'assets/snacks/cola.png',     200, 'ON'),
    ('Nachos',    4.50, 'assets/snacks/nachos.png',   80,  'ON');

-- =============================================================
-- 7. 定价规则（与原系统定价逻辑对应）
-- =============================================================
INSERT INTO pricing_rule (rule_name, rule_type, rule_value, priority, enabled, description)
VALUES
    ('会员折扣 8折',        'MEMBER_DISCOUNT',   0.80, 10, TRUE,  '会员享受 8 折优惠'),
    ('周二半价',             'TUESDAY_DISCOUNT',  0.50, 20, TRUE,  '每周二所有票价半价'),
    ('黄金时段加价 15%',    'PEAK_SURCHARGE',    1.15, 5,  TRUE,  '18:00-22:00 黄金时段加价 15%'),
    ('节假日折扣 8折',      'HOLIDAY_DISCOUNT',  0.80, 15, TRUE,  '节假日（如圣诞节）享受 8 折'),
    ('高销量加价 10%',      'SOLD_OUT_SURCHARGE',1.10, 5,  TRUE,  '销量超过 100 张时加价 10%'),
    ('低销量折扣 9折',      'LOW_SALES_DISCOUNT',0.90, 5,  TRUE,  '销量低于 30 张时折扣 9 折');
