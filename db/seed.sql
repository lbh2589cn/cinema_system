-- =============================================================
-- 电影院购票系统 - 初始化种子数据（演示版）
-- 说明: 用于开发/演示环境初始化，包含大量随机但合理的数据
-- 注意: 生产环境请勿自动执行此脚本
-- =============================================================

-- =============================================================
-- 1. 用户
--    密码均为 "123456" 的 BCrypt 哈希
-- =============================================================
INSERT INTO `user` (user_id, username, password_hash, phone, nickname, role, is_member, `status`)
VALUES
    ('admin',     'Admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000000', '管理员', 'ADMIN', TRUE,  'ACTIVE'),
    ('alice',     'Alice',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000001', '爱丽丝', 'USER',  TRUE,  'ACTIVE'),
    ('bob',       'Bob',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000002', '鲍勃',   'USER',  FALSE, 'ACTIVE'),
    ('charlie',   'Charlie',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000003', '查理',   'USER',  TRUE,  'ACTIVE'),
    ('diana',     'Diana',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000004', '戴安娜', 'USER',  FALSE, 'ACTIVE'),
    ('eve',       'Eve',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800000005', '小伊',   'USER',  TRUE,  'ACTIVE');

-- =============================================================
-- 2. 影厅
-- =============================================================
INSERT INTO hall (name, `rows`, `cols`, seat_count, description)
VALUES
    ('Standard', 8,  12, 96,  '标准厅：8行 × 12列，共96座'),
    ('IMAX',    15, 20, 300, 'IMAX巨幕厅：15行 × 20列，共300座'),
    ('VIP',      4,  5,  20,  'VIP厅：4行 × 5列，共20座，真皮沙发可躺平');

-- =============================================================
-- 3. 座位布局（根据影厅自动生成）
-- =============================================================

-- Standard 厅：8行×12列
INSERT INTO hall_seat (hall_id, row_num, col_num, seat_type)
WITH RECURSIVE rows_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM rows_gen WHERE n < 8
),
cols_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM cols_gen WHERE n < 12
)
SELECT 1, r.n, c.n, 'STANDARD'
FROM rows_gen r, cols_gen c;

-- IMAX 厅：15行×20列，中间区域设为 VIP 座（第6-10行, 第7-14列）
INSERT INTO hall_seat (hall_id, row_num, col_num, seat_type)
WITH RECURSIVE rows_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM rows_gen WHERE n < 15
),
cols_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM cols_gen WHERE n < 20
)
SELECT 2, r.n, c.n,
    CASE WHEN r.n BETWEEN 6 AND 10 AND c.n BETWEEN 7 AND 14 THEN 'VIP' ELSE 'STANDARD' END
FROM rows_gen r, cols_gen c;

-- VIP 厅：4行×5列，全部 VIP
INSERT INTO hall_seat (hall_id, row_num, col_num, seat_type)
WITH RECURSIVE rows_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM rows_gen WHERE n < 4
),
cols_gen(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM cols_gen WHERE n < 5
)
SELECT 3, r.n, c.n, 'VIP'
FROM rows_gen r, cols_gen c;

-- =============================================================
-- 4. 电影
-- =============================================================
INSERT INTO movie (title, poster_url, description, duration, rating, genre, release_date, `status`)
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

    ('Zootopia 2',
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
     'The story of Forrest Gump, a man with a low IQ but a kind heart, who unwittingly influences several historical events.',
     142, 8.8, 'Drama,Romance',
     '2026-04-01', 'ON'),

    ('Harry Potter and the Sorcerer''s Stone',
     'assets/posters/harry_potter.jpg',
     'An epic fantasy series following the journey of a young wizard, Harry Potter, and his friends as they battle dark forces.',
     152, 8.5, 'Fantasy,Adventure',
     '2026-04-01', 'ON'),

    ('The Matrix',
     'assets/posters/matrix.jpg',
     'A computer hacker learns about the true nature of reality and his role in the war against the machines that control it.',
     136, 8.7, 'Sci-Fi,Action',
     '2026-05-01', 'ON'),

    ('Spirited Away',
     'assets/posters/spirited_away.jpg',
     'During her family''s move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits.',
     125, 8.6, 'Animation,Fantasy,Adventure',
     '2026-05-01', 'ON');

-- =============================================================
-- 5. 排片（生成 5 天 × 每天 10 场 = 50 场）
--    覆盖不同时间段和不同影厅
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
    -- 轮换影厅: 根据 (movie_id + day_offset + slot) 计算
    ((m.id + DATEDIFF(d.d, '2026-07-15') + ts.slot_idx - 1) % 3) + 1,
    d.d,
    ts.t,
    -- 票价：基础价 + 电影系数 + 时段加价
    15.0 + (m.id * 2.5)
        + CASE WHEN ts.slot_idx IN (4, 5) THEN 8.0 ELSE 0 END      -- 黄金时段加价
        + CASE WHEN ts.slot_idx = 1 THEN -3.0 ELSE 0 END,           -- 早场优惠
    CASE
        WHEN d.d < '2026-07-17' AND ts.slot_idx <= 2 THEN 'FINISHED'
        WHEN d.d = '2026-07-18' AND ts.slot_idx <= 2 THEN 'ONGOING'
        ELSE 'SCHEDULED'
    END
FROM movie m, dates d, time_slots ts
-- 每部电影每天最多 3 个时段（而不是全部5个），避免数据过多
WHERE ts.slot_idx <= 3 + ((m.id + DATEDIFF(d.d, '2026-07-15')) % 3);

-- =============================================================
-- 6. 为每场放映生成座位预订记录
--    这是核心数据：每个场次的每个座位一条记录
-- =============================================================
INSERT INTO seat_booking (showing_id, seat_id, `status`, version)
SELECT s.id, hs.id, 'AVAILABLE', 0
FROM showing s
JOIN hall_seat hs ON hs.hall_id = s.hall_id;

-- 将一部分已结束场次的座位标记为 BOOKED（模拟已售出）
-- 影响 20% 的座位，使统计数据和仪表盘有内容可展示
UPDATE seat_booking sb
JOIN showing s ON s.id = sb.showing_id
SET sb.status = 'BOOKED',
    sb.booked_by = FLOOR(1 + RAND() * 5),          -- 随机分配用户 1-5
    sb.booked_at = DATE_ADD(s.show_date, INTERVAL -1 DAY),
    sb.order_id = s.id  -- 临时用 showing_id 占位
WHERE s.status = 'FINISHED'
  AND sb.id % 5 = 0;   -- 每 5 个选 1 个

-- 将部分场次标记少量 LOCKED（模拟锁定中，15分钟超时）
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
-- 7. 订单 + 明细 + 支付（生成 5 个示例订单）
-- =============================================================

-- 7a. 订单主表
INSERT INTO `order` (order_no, user_id, showing_id, seat_count, total_amount, discount_amount, final_amount, `status`, remark, created_at)
VALUES
    ('20260715000001', 2, 1,  2,  96.00,  19.20,  76.80,  'PAID',  NULL,    '2026-07-14 14:30:00'),
    ('20260715000002', 3, 3,  3,  144.00, 0,       144.00, 'PAID',  NULL,    '2026-07-14 15:00:00'),
    ('20260716000003', 4, 12, 1,  65.00,  13.00,  52.00,  'PAID',  NULL,    '2026-07-15 10:15:00'),
    ('20260717000004', 5, 25, 4,  320.00, 0,       320.00, 'PAID',  NULL,    '2026-07-16 20:00:00'),
    ('20260718000005', 2, 35, 2,  88.00,  17.60,  70.40,  'REFUNDED', '临时有事取消', '2026-07-17 09:00:00');

-- 7b. 更新 seat_booking 关联到真实订单
-- 找到已售座位且没有 order_id 的记录，关联到对应订单
UPDATE seat_booking sb
JOIN `order` o ON o.showing_id = sb.showing_id
SET sb.order_id = o.id
WHERE sb.status = 'BOOKED' AND sb.order_id IS NULL
  AND sb.booked_by = o.user_id;

-- 订单 5（REFUNDED）对应的座位释放回 AVAILABLE
UPDATE seat_booking sb
SET sb.status = 'AVAILABLE',
    sb.booked_by = NULL,
    sb.booked_at = NULL,
    sb.order_id = NULL
WHERE sb.order_id = 5;

-- 7c. 订单明细（座位项）
INSERT INTO order_item (order_id, item_type, item_id, item_name, quantity, unit_price, subtotal)
SELECT o.id, 'SEAT', sb.id, CONCAT('座位 ', hs.row_num, '-', hs.col_num), 1,
       (o.total_amount / o.seat_count),
       (o.total_amount / o.seat_count)
FROM `order` o
JOIN seat_booking sb ON sb.order_id = o.id
JOIN hall_seat hs ON hs.id = sb.seat_id
WHERE o.status = 'PAID';

-- 7d. 订单明细（零食项）- 为部分订单加点零食
INSERT INTO order_item (order_id, item_type, item_id, item_name, quantity, unit_price, subtotal)
VALUES
    (1, 'SNACK', 1, 'Popcorn',   2, 3.00,  6.00),
    (1, 'SNACK', 2, 'Coca-Cola', 2, 2.00,  4.00),
    (2, 'SNACK', 3, 'Nachos',    3, 4.50,  13.50),
    (4, 'SNACK', 1, 'Popcorn',   4, 3.00,  12.00),
    (4, 'SNACK', 2, 'Coca-Cola', 4, 2.00,  8.00);

-- 7e. 支付记录
INSERT INTO payment (order_id, payment_no, amount, payment_method, `status`, paid_at, created_at)
VALUES
    (1, 'PAY202607150001', 76.80,  'WECHAT', 'SUCCESS', '2026-07-14 14:31:00', '2026-07-14 14:30:30'),
    (2, 'PAY202607150002', 144.00, 'ALIPAY', 'SUCCESS', '2026-07-14 15:01:00', '2026-07-14 15:00:30'),
    (3, 'PAY202607160003', 52.00,  'WECHAT', 'SUCCESS', '2026-07-15 10:16:00', '2026-07-15 10:15:30'),
    (4, 'PAY202607170004', 320.00, 'ALIPAY', 'SUCCESS', '2026-07-16 20:01:00', '2026-07-16 20:00:30');

-- =============================================================
-- 8. 零食（5款）
-- =============================================================
INSERT INTO snack (name, price, image_url, stock, `status`)
VALUES
    ('Popcorn',   3.00, 'assets/snacks/popcorn.png', 100, 'ON'),
    ('Coca-Cola', 2.00, 'assets/snacks/cola.png',     200, 'ON'),
    ('Nachos',    4.50, 'assets/snacks/nachos.png',   80,  'ON'),
    ('Sprite',    2.00, 'assets/snacks/sprite.png',   150, 'ON'),
    ('M&M''s',    3.50, 'assets/snacks/mms.png',      120, 'ON');

-- =============================================================
-- 9. 定价规则
-- =============================================================
INSERT INTO pricing_rule (rule_name, rule_type, rule_value, priority, enabled, description)
VALUES
    ('会员折扣 8折',        'MEMBER_DISCOUNT',   0.80, 10, TRUE,  '会员享受 8 折优惠'),
    ('周二半价',             'TUESDAY_DISCOUNT',  0.50, 20, TRUE,  '每周二所有票价半价'),
    ('黄金时段加价 15%',    'PEAK_SURCHARGE',    1.15, 5,  TRUE,  '18:00-22:00 黄金时段加价 15%'),
    ('节假日折扣 8折',      'HOLIDAY_DISCOUNT',  0.80, 15, TRUE,  '节假日享受 8 折'),
    ('高销量加价 10%',      'SOLD_OUT_SURCHARGE',1.10, 5,  TRUE,  '销量超过 100 张时加价 10%'),
    ('低销量折扣 9折',      'LOW_SALES_DISCOUNT',0.90, 5,  TRUE,  '销量低于 30 张时折扣 9 折');

-- =============================================================
-- 10. 汇总信息输出（控制台）
-- =============================================================
SELECT '种子数据加载完成!' AS message;
SELECT CONCAT('用户: ', COUNT(*)) AS info FROM `user`;
SELECT CONCAT('电影: ', COUNT(*)) AS info FROM movie;
SELECT CONCAT('影厅: ', COUNT(*), ' (共 ', SUM(seat_count), ' 座)') AS info FROM hall;
SELECT CONCAT('座位: ', COUNT(*)) AS info FROM hall_seat;
SELECT CONCAT('排片: ', COUNT(*)) AS info FROM showing;
SELECT CONCAT('座位预订: ', COUNT(*), ' (可售:', SUM(CASE WHEN `status`='AVAILABLE' THEN 1 ELSE 0 END),
    ', 已锁定:', SUM(CASE WHEN `status`='LOCKED' THEN 1 ELSE 0 END),
    ', 已售出:', SUM(CASE WHEN `status`='BOOKED' THEN 1 ELSE 0 END), ')') AS info FROM seat_booking;
SELECT CONCAT('订单: ', COUNT(*)) AS info FROM `order`;
SELECT CONCAT('零食: ', COUNT(*)) AS info FROM snack;
SELECT CONCAT('定价规则: ', COUNT(*)) AS info FROM pricing_rule;
