-- =============================================================
-- 1. 用户表
--    存储系统用户信息，包括普通用户和管理员
-- =============================================================
CREATE TABLE IF NOT EXISTS `user` (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id         VARCHAR(50)         NOT NULL COMMENT '登录账号（唯一）',
    username        VARCHAR(50)         NOT NULL COMMENT '用户昵称/显示名',
    password_hash   VARCHAR(255)        NOT NULL COMMENT 'BCrypt 加密后的密码',
    phone           VARCHAR(20)         COMMENT '手机号',
    role            VARCHAR(10)         NOT NULL DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
    is_member       BOOLEAN             NOT NULL DEFAULT FALSE COMMENT '是否会员',
    status          VARCHAR(10)         NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / DISABLED',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_user_id UNIQUE (user_id),
    CONSTRAINT ck_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_user_phone ON `user` (phone);
CREATE INDEX idx_user_status ON `user` (status);


-- =============================================================
-- 2. 电影表
--    存储电影元数据（名称、海报、简介、评分等）
-- =============================================================
CREATE TABLE IF NOT EXISTS movie (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title           VARCHAR(100)        NOT NULL COMMENT '电影名称',
    poster_url      VARCHAR(255)        COMMENT '海报图片 URL',
    description     TEXT                COMMENT '剧情简介',
    duration        INT                 NOT NULL COMMENT '时长（分钟）',
    rating          DECIMAL(3,1)        DEFAULT 0.0 COMMENT '评分（0.0-10.0）',
    genre           VARCHAR(100)        COMMENT '类型（逗号分隔）',
    release_date    DATE                COMMENT '上映日期',
    status          VARCHAR(10)         NOT NULL DEFAULT 'ON' COMMENT '状态: ON / OFF',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT ck_movie_rating CHECK (rating >= 0 AND rating <= 10),
    CONSTRAINT ck_movie_duration CHECK (duration > 0),
    CONSTRAINT ck_movie_status CHECK (status IN ('ON', 'OFF'))
);

CREATE INDEX idx_movie_title ON movie (title);
CREATE INDEX idx_movie_genre ON movie (genre);
CREATE INDEX idx_movie_status ON movie (status);
CREATE INDEX idx_movie_release_date ON movie (release_date);


-- =============================================================
-- 3. 影厅表
--    定义影厅的基础信息（名称、座位布局）
-- =============================================================
CREATE TABLE IF NOT EXISTS hall (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name            VARCHAR(50)         NOT NULL COMMENT '影厅名称',
    `rows`          INT                 NOT NULL COMMENT '座位行数',
    `cols`          INT                 NOT NULL COMMENT '座位列数',
    seat_count      INT                 NOT NULL COMMENT '座位总数',
    description     VARCHAR(255)        COMMENT '影厅描述',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_hall_rows CHECK (`rows` > 0),
    CONSTRAINT ck_hall_cols CHECK (`cols` > 0),
    CONSTRAINT ck_hall_seat_count CHECK (seat_count > 0)
);


-- =============================================================
-- 4. 座位表
--    每个影厅的固定座位布局
-- =============================================================
CREATE TABLE IF NOT EXISTS hall_seat (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    hall_id         BIGINT              NOT NULL COMMENT '所属影厅',
    row_num         INT                 NOT NULL COMMENT '行号（从 1 开始）',
    col_num         INT                 NOT NULL COMMENT '列号（从 1 开始）',
    status          VARCHAR(20)         NOT NULL DEFAULT 'STANDARD' COMMENT '座位状态',
    CONSTRAINT uk_hall_seat UNIQUE (hall_id, row_num, col_num),
    CONSTRAINT ck_hall_seat_status CHECK (status IN ('STANDARD', 'VIP', 'UNAVAILABLE')),
    CONSTRAINT ck_hall_seat_row CHECK (row_num > 0),
    CONSTRAINT ck_hall_seat_col CHECK (col_num > 0)
);

CREATE INDEX idx_hall_seat_hall_id ON hall_seat (hall_id);


-- =============================================================
-- 5. 排片表
--    电影在特定影厅的放映场次
-- =============================================================
CREATE TABLE IF NOT EXISTS showing (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    movie_id        BIGINT              NOT NULL COMMENT '电影ID',
    hall_id         BIGINT              NOT NULL COMMENT '影厅ID',
    show_date       DATE                NOT NULL COMMENT '放映日期',
    show_time       TIME                NOT NULL COMMENT '放映时间',
    base_price      DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT '基础票价',
    status          VARCHAR(20)         NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_showing UNIQUE (movie_id, hall_id, show_date, show_time),
    CONSTRAINT ck_showing_base_price CHECK (base_price >= 0),
    CONSTRAINT ck_showing_status CHECK (status IN ('SCHEDULED', 'CANCELLED'))
);

CREATE INDEX idx_showing_date ON showing (show_date);
CREATE INDEX idx_showing_movie_id ON showing (movie_id);
CREATE INDEX idx_showing_hall_id ON showing (hall_id);
CREATE INDEX idx_showing_status ON showing (status);
CREATE INDEX idx_showing_movie_date ON showing (movie_id, show_date);


-- =============================================================
-- 6. 座位预订表
--    每场演出中每个座位的预订状态
-- =============================================================
CREATE TABLE IF NOT EXISTS seat_booking (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    showing_id      BIGINT              NOT NULL COMMENT '排片ID',
    seat_id         BIGINT              NOT NULL COMMENT '座位ID',
    status          VARCHAR(20)         NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态',
    locked_by       BIGINT              COMMENT '锁定人（用户ID）',
    locked_at       TIMESTAMP           COMMENT '锁定时间',
    booked_by       BIGINT              COMMENT '预订人（用户ID）',
    booked_at       TIMESTAMP           COMMENT '预订时间',
    order_id        BIGINT              COMMENT '所属订单ID',
    version         INT                 NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    CONSTRAINT uk_showing_seat UNIQUE (showing_id, seat_id),
    CONSTRAINT ck_seat_booking_status CHECK (status IN ('AVAILABLE', 'LOCKED', 'BOOKED'))
);

CREATE INDEX idx_seat_booking_showing_id ON seat_booking (showing_id);
CREATE INDEX idx_seat_booking_status ON seat_booking (status);
CREATE INDEX idx_seat_booking_locked_by ON seat_booking (locked_by);
CREATE INDEX idx_seat_booking_booked_by ON seat_booking (booked_by);
CREATE INDEX idx_seat_booking_order_id ON seat_booking (order_id);
CREATE INDEX idx_seat_booking_locked_at ON seat_booking (locked_at);


-- =============================================================
-- 7. 订单表
--    用户购票订单主表
-- =============================================================
CREATE TABLE IF NOT EXISTS `order` (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no        VARCHAR(32)         NOT NULL COMMENT '订单号',
    user_id         BIGINT              NOT NULL COMMENT '用户ID',
    showing_id      BIGINT              NOT NULL COMMENT '排片ID',
    seat_count      INT                 NOT NULL DEFAULT 0 COMMENT '座位数量',
    total_amount    DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT '总金额',
    discount_amount DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    final_amount    DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    remark          VARCHAR(255)        COMMENT '备注',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_order_no UNIQUE (order_no),
    CONSTRAINT ck_order_seat_count CHECK (seat_count > 0),
    CONSTRAINT ck_order_amounts CHECK (total_amount >= 0 AND discount_amount >= 0 AND final_amount >= 0),
    CONSTRAINT ck_order_status CHECK (status IN ('PENDING', 'PAID', 'REFUNDING', 'REFUNDED', 'CANCELLED'))
);

CREATE INDEX idx_order_user_id ON `order` (user_id);
CREATE INDEX idx_order_showing_id ON `order` (showing_id);
CREATE INDEX idx_order_status ON `order` (status);
CREATE INDEX idx_order_created_at ON `order` (created_at DESC);


-- =============================================================
-- 8. 订单明细表
--    存储订单包含的每个项目（座位和零食）
-- =============================================================
CREATE TABLE IF NOT EXISTS order_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id        BIGINT              NOT NULL COMMENT '所属订单ID',
    item_type       VARCHAR(10)         NOT NULL COMMENT '项目类型: SEAT / SNACK',
    item_id         BIGINT              NOT NULL COMMENT '项目 ID',
    item_name       VARCHAR(100)        NOT NULL COMMENT '项目名称',
    quantity        INT                 NOT NULL DEFAULT 1 COMMENT '数量',
    unit_price      DECIMAL(10, 2)      NOT NULL COMMENT '单价',
    subtotal        DECIMAL(10, 2)      NOT NULL COMMENT '小计',

    CONSTRAINT ck_order_item_type CHECK (item_type IN ('SEAT', 'SNACK')),
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_item_prices CHECK (unit_price >= 0 AND subtotal >= 0)
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);


-- =============================================================
-- 9. 支付记录表
--    记录每笔支付的流水信息
-- =============================================================
CREATE TABLE IF NOT EXISTS payment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id        BIGINT              NOT NULL COMMENT '关联订单ID',
    payment_no      VARCHAR(32)         NOT NULL COMMENT '支付流水号',
    amount          DECIMAL(10, 2)      NOT NULL COMMENT '支付金额',
    payment_method  VARCHAR(20)         COMMENT '支付方式',
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    paid_at         TIMESTAMP           COMMENT '支付完成时间',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_payment_no UNIQUE (payment_no),
    CONSTRAINT ck_payment_amount CHECK (amount > 0),
    CONSTRAINT ck_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    CONSTRAINT ck_payment_method CHECK (payment_method IN ('WECHAT', 'ALIPAY', 'CREDIT_CARD', 'BALANCE'))
);

CREATE INDEX idx_payment_order_id ON payment (order_id);


-- =============================================================
-- 10. 零食表
--    影院可售卖的零食饮品
-- =============================================================
CREATE TABLE IF NOT EXISTS snack (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name            VARCHAR(50)         NOT NULL COMMENT '零食名称',
    price           DECIMAL(10, 2)      NOT NULL COMMENT '单价',
    image_url       VARCHAR(255)        COMMENT '图片 URL',
    stock           INT                 NOT NULL DEFAULT 0 COMMENT '当前库存',
    status          VARCHAR(10)         NOT NULL DEFAULT 'ON' COMMENT '状态: ON / OFF',

    CONSTRAINT ck_snack_price CHECK (price > 0),
    CONSTRAINT ck_snack_stock CHECK (stock >= 0),
    CONSTRAINT ck_snack_status CHECK (status IN ('ON', 'OFF'))
);

CREATE INDEX idx_snack_status ON snack (status);


-- =============================================================
-- 11. 定价规则表
--    动态定价/折扣规则配置
-- =============================================================
CREATE TABLE IF NOT EXISTS pricing_rule (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_name       VARCHAR(100)        NOT NULL COMMENT '规则名称',
    rule_value      DECIMAL(10, 4)      NOT NULL COMMENT '规则值（折扣率/加价率）',
    priority        INT                 NOT NULL DEFAULT 0 COMMENT '应用优先级',
    enabled         BOOLEAN             NOT NULL DEFAULT TRUE COMMENT '是否启用',
    description     VARCHAR(255)        COMMENT '规则说明',

    condition_member      BOOLEAN       COMMENT '条件：是否为会员',
    condition_weekdays    VARCHAR(50)   COMMENT '条件：星期几',
    condition_time_start  TIME          COMMENT '条件：开始时间范围-开始',
    condition_time_end    TIME          COMMENT '条件：开始时间范围-结束',
    condition_ticket_min  INT           COMMENT '条件：购买票数-最小',
    condition_ticket_max  INT           COMMENT '条件：购买票数-最大',
    condition_seat_ratio_min  DECIMAL(5,2) COMMENT '条件：剩余座位比例-最小',
    condition_seat_ratio_max  DECIMAL(5,2) COMMENT '条件：剩余座位比例-最大',

    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT ck_pricing_rule_value CHECK (rule_value > 0),
    CONSTRAINT ck_pricing_rule_seat_ratio CHECK (
        (condition_seat_ratio_min IS NULL OR (condition_seat_ratio_min >= 0 AND condition_seat_ratio_min <= 1))
        AND (condition_seat_ratio_max IS NULL OR (condition_seat_ratio_max >= 0 AND condition_seat_ratio_max <= 1))
    )
);

CREATE INDEX idx_pricing_rule_enabled ON pricing_rule (enabled);