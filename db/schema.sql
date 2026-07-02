-- =============================================================
-- 电影院购票系统 - 数据库 Schema
-- 数据库: PostgreSQL 15+
-- 说明: 包含 11 张业务表，覆盖用户、电影、影厅、排片、
--       座位、零食、订单、支付、定价等完整业务域
--       不含外键约束，关联关系在业务层维护
-- =============================================================

-- =============================================================
-- 1. 用户表
--    存储系统用户信息，包括普通用户和管理员
-- =============================================================
CREATE TABLE IF NOT EXISTS "user" (
    id              BIGSERIAL           PRIMARY KEY,
    user_id         VARCHAR(50)         NOT NULL,                   -- 登录账号（唯一）
    username        VARCHAR(50)         NOT NULL,                   -- 用户昵称/显示名
    password_hash   VARCHAR(255)        NOT NULL,                   -- BCrypt 加密后的密码
    phone           VARCHAR(20),                                    -- 手机号
    nickname        VARCHAR(50),                                    -- 昵称（可选）
    role            VARCHAR(10)         NOT NULL DEFAULT 'USER',    -- 角色: USER / ADMIN
    is_member       BOOLEAN             NOT NULL DEFAULT FALSE,     -- 是否会员
    status          VARCHAR(10)         NOT NULL DEFAULT 'ACTIVE',  -- 状态: ACTIVE / DISABLED
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_user_user_id UNIQUE (user_id),
    CONSTRAINT ck_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_user_phone ON "user" (phone);
CREATE INDEX idx_user_status ON "user" (status);

COMMENT ON TABLE "user" IS '用户表：存储系统用户信息';
COMMENT ON COLUMN "user".user_id IS '登录账号，业务唯一标识';
COMMENT ON COLUMN "user".password_hash IS 'BCrypt 哈希值，不可逆存储';
COMMENT ON COLUMN "user".role IS '用户角色：USER-普通用户，ADMIN-管理员';
COMMENT ON COLUMN "user".is_member IS '会员标识，影响票价折扣';
COMMENT ON COLUMN "user".status IS '账户状态：ACTIVE-正常，DISABLED-禁用';


-- =============================================================
-- 2. 电影表
--    存储电影元数据（名称、海报、简介、评分等）
-- =============================================================
CREATE TABLE IF NOT EXISTS movie (
    id              BIGSERIAL           PRIMARY KEY,
    title           VARCHAR(100)        NOT NULL,                   -- 电影名称
    poster_url      VARCHAR(255),                                   -- 海报图片 URL
    description     TEXT,                                           -- 剧情简介
    duration        INT                 NOT NULL,                   -- 时长（分钟）
    rating          DECIMAL(3,1)        DEFAULT 0.0,                -- 评分（0.0-10.0）
    genre           VARCHAR(100),                                   -- 类型（逗号分隔，如 "Sci-Fi,Action"）
    release_date    DATE,                                           -- 上映日期
    status          VARCHAR(10)         NOT NULL DEFAULT 'ON',      -- 状态: ON / OFF
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_movie_rating CHECK (rating >= 0 AND rating <= 10),
    CONSTRAINT ck_movie_duration CHECK (duration > 0),
    CONSTRAINT ck_movie_status CHECK (status IN ('ON', 'OFF'))
);

CREATE INDEX idx_movie_title ON movie (title);
CREATE INDEX idx_movie_genre ON movie (genre);
CREATE INDEX idx_movie_status ON movie (status);
CREATE INDEX idx_movie_release_date ON movie (release_date);

COMMENT ON TABLE movie IS '电影表：存储电影元数据';
COMMENT ON COLUMN movie.title IS '电影名称，支持模糊搜索';
COMMENT ON COLUMN movie.poster_url IS '海报图片的相对路径或URL';
COMMENT ON COLUMN movie.genre IS '类型标签，用逗号分隔多个类型';
COMMENT ON COLUMN movie.status IS '上映状态：ON-正在上映，OFF-已下架';


-- =============================================================
-- 3. 影厅表
--    定义影厅的基础信息（名称、座位布局）
-- =============================================================
CREATE TABLE IF NOT EXISTS hall (
    id              BIGSERIAL           PRIMARY KEY,
    name            VARCHAR(50)         NOT NULL,                   -- 影厅名称（Standard / IMAX / VIP）
    rows            INT                 NOT NULL,                   -- 座位行数
    cols            INT                 NOT NULL,                   -- 座位列数（左右两侧之和）
    seat_count      INT                 NOT NULL,                   -- 座位总数
    description     VARCHAR(255),                                   -- 影厅描述
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_hall_rows CHECK (rows > 0),
    CONSTRAINT ck_hall_cols CHECK (cols > 0),
    CONSTRAINT ck_hall_seat_count CHECK (seat_count > 0)
);

COMMENT ON TABLE hall IS '影厅表：定义影厅的座位布局和基本信息';
COMMENT ON COLUMN hall.rows IS '座位行数，如 IMAX 为 15 行';
COMMENT ON COLUMN hall.cols IS '座位列数（含左右两侧总列数）';


-- =============================================================
-- 4. 座位表
--    每个影厅的固定座位布局，影厅创建时自动生成
-- =============================================================
CREATE TABLE IF NOT EXISTS hall_seat (
    id              BIGSERIAL           PRIMARY KEY,
    hall_id         BIGINT              NOT NULL,                   -- 所属影厅（业务关联）
    row_num         INT                 NOT NULL,                   -- 行号（从 1 开始）
    col_num         INT                 NOT NULL,                   -- 列号（从 1 开始）
    seat_type       VARCHAR(20)         NOT NULL DEFAULT 'STANDARD',-- 座位类型: STANDARD / VIP / WHEELCHAIR

    CONSTRAINT uk_hall_seat UNIQUE (hall_id, row_num, col_num),
    CONSTRAINT ck_seat_type CHECK (seat_type IN ('STANDARD', 'VIP', 'WHEELCHAIR')),
    CONSTRAINT ck_hall_seat_row CHECK (row_num > 0),
    CONSTRAINT ck_hall_seat_col CHECK (col_num > 0)
);

CREATE INDEX idx_hall_seat_hall_id ON hall_seat (hall_id);

COMMENT ON TABLE hall_seat IS '座位表：影厅的固定座位布局';
COMMENT ON COLUMN hall_seat.seat_type IS '座位类型：STANDARD-标准座，VIP-VIP座，WHEELCHAIR-无障碍座';


-- =============================================================
-- 5. 排片表
--    电影在特定影厅的放映场次
-- =============================================================
CREATE TABLE IF NOT EXISTS showing (
    id              BIGSERIAL           PRIMARY KEY,
    movie_id        BIGINT              NOT NULL,                   -- 电影ID（业务关联）
    hall_id         BIGINT              NOT NULL,                   -- 影厅ID（业务关联）
    show_date       DATE                NOT NULL,                   -- 放映日期
    show_time       TIME                NOT NULL,                   -- 放映时间
    base_price      DECIMAL(10, 2)      NOT NULL DEFAULT 0.00,      -- 基础票价
    status          VARCHAR(20)         NOT NULL DEFAULT 'SCHEDULED', -- 状态: SCHEDULED / ONGOING / FINISHED / CANCELLED
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_showing UNIQUE (movie_id, hall_id, show_date, show_time),
    CONSTRAINT ck_showing_base_price CHECK (base_price >= 0),
    CONSTRAINT ck_showing_status CHECK (status IN ('SCHEDULED', 'ONGOING', 'FINISHED', 'CANCELLED'))
);

CREATE INDEX idx_showing_date ON showing (show_date);
CREATE INDEX idx_showing_movie_id ON showing (movie_id);
CREATE INDEX idx_showing_hall_id ON showing (hall_id);
CREATE INDEX idx_showing_status ON showing (status);
CREATE INDEX idx_showing_movie_date ON showing (movie_id, show_date);

COMMENT ON TABLE showing IS '排片表：电影在特定影厅的放映场次';
COMMENT ON COLUMN showing.base_price IS '基础票价，最终价格由定价规则计算得出';
COMMENT ON COLUMN showing.status IS '场次状态：SCHEDULED-待放映，ONGOING-放映中，FINISHED-已结束，CANCELLED-已取消';


-- =============================================================
-- 6. 座位预订表
--    每场演出中每个座位的预订状态，核心高并发表
-- =============================================================
CREATE TABLE IF NOT EXISTS seat_booking (
    id              BIGSERIAL           PRIMARY KEY,
    showing_id      BIGINT              NOT NULL,                   -- 排片ID（业务关联）
    seat_id         BIGINT              NOT NULL,                   -- 座位ID（业务关联）
    status          VARCHAR(20)         NOT NULL DEFAULT 'AVAILABLE', -- 状态: AVAILABLE / LOCKED / BOOKED
    locked_by       BIGINT,                                         -- 锁定人（用户ID，业务关联）
    locked_at       TIMESTAMP,                                      -- 锁定时间（用于超时释放）
    booked_by       BIGINT,                                         -- 预订人（用户ID，业务关联）
    booked_at       TIMESTAMP,                                      -- 预订时间
    order_id        BIGINT,                                         -- 所属订单ID（业务关联）
    version         INT                 NOT NULL DEFAULT 0,         -- 乐观锁版本号

    CONSTRAINT uk_showing_seat UNIQUE (showing_id, seat_id),
    CONSTRAINT ck_seat_booking_status CHECK (status IN ('AVAILABLE', 'LOCKED', 'BOOKED'))
);

CREATE INDEX idx_seat_booking_showing_id ON seat_booking (showing_id);
CREATE INDEX idx_seat_booking_status ON seat_booking (status);
CREATE INDEX idx_seat_booking_locked_by ON seat_booking (locked_by);
CREATE INDEX idx_seat_booking_booked_by ON seat_booking (booked_by);
CREATE INDEX idx_seat_booking_order_id ON seat_booking (order_id);
-- 用于定时任务查询超时锁定的座位
CREATE INDEX idx_seat_booking_locked_at ON seat_booking (locked_at)
    WHERE status = 'LOCKED';

COMMENT ON TABLE seat_booking IS '座位预订表：核心高并发表，记录每场演出每个座位状态';
COMMENT ON COLUMN seat_booking.status IS '座位状态：AVAILABLE-可售，LOCKED-已锁定（15分钟超时），BOOKED-已售出';
COMMENT ON COLUMN seat_booking.locked_at IS '锁定时间，定时任务据此释放超时座位';
COMMENT ON COLUMN seat_booking.version IS '乐观锁版本号，用于并发控制';


-- =============================================================
-- 7. 订单表
--    用户购票订单主表
-- =============================================================
CREATE TABLE IF NOT EXISTS "order" (
    id                BIGSERIAL           PRIMARY KEY,
    order_no          VARCHAR(32)         NOT NULL,                 -- 订单号（业务唯一）
    user_id           BIGINT              NOT NULL,                 -- 用户ID（业务关联）
    showing_id        BIGINT              NOT NULL,                 -- 排片ID（业务关联）
    seat_count        INT                 NOT NULL DEFAULT 0,       -- 座位数量
    total_amount      DECIMAL(10, 2)      NOT NULL DEFAULT 0.00,    -- 总金额（折扣前）
    discount_amount   DECIMAL(10, 2)      NOT NULL DEFAULT 0.00,    -- 优惠金额
    final_amount      DECIMAL(10, 2)      NOT NULL DEFAULT 0.00,    -- 实付金额
    status            VARCHAR(20)         NOT NULL DEFAULT 'PENDING', -- 状态: PENDING / PAID / REFUNDING / REFUNDED / CANCELLED
    remark            VARCHAR(255),                                   -- 备注
    created_at        TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_order_no UNIQUE (order_no),
    CONSTRAINT ck_order_seat_count CHECK (seat_count > 0),
    CONSTRAINT ck_order_amounts CHECK (
        total_amount >= 0 AND discount_amount >= 0 AND final_amount >= 0
    ),
    CONSTRAINT ck_order_status CHECK (
        status IN ('PENDING', 'PAID', 'REFUNDING', 'REFUNDED', 'CANCELLED')
    )
);

CREATE INDEX idx_order_user_id ON "order" (user_id);
CREATE INDEX idx_order_showing_id ON "order" (showing_id);
CREATE INDEX idx_order_status ON "order" (status);
CREATE INDEX idx_order_created_at ON "order" (created_at DESC);

COMMENT ON TABLE "order" IS '订单表：用户购票订单主表';
COMMENT ON COLUMN "order".order_no IS '业务订单号，格式规则：yyyyMMdd + 8位序列';
COMMENT ON COLUMN "order".total_amount IS '折扣前总金额（座位票价合计 + 零食合计）';
COMMENT ON COLUMN "order".discount_amount IS '优惠金额（会员折扣、周二折扣等）';
COMMENT ON COLUMN "order".final_amount IS '实付金额 = total_amount - discount_amount';
COMMENT ON COLUMN "order".status IS '订单状态：PENDING-待支付，PAID-已支付，REFUNDING-退款中，REFUNDED-已退款，CANCELLED-已取消';


-- =============================================================
-- 8. 订单明细表
--    存储订单包含的每个项目（座位和零食）
-- =============================================================
CREATE TABLE IF NOT EXISTS order_item (
    id              BIGSERIAL           PRIMARY KEY,
    order_id        BIGINT              NOT NULL,                   -- 所属订单ID（业务关联）
    item_type       VARCHAR(10)         NOT NULL,                   -- 项目类型: SEAT / SNACK
    item_id         BIGINT              NOT NULL,                   -- 项目 ID（seat_booking.id 或 snack.id）
    item_name       VARCHAR(100)        NOT NULL,                   -- 项目名称（快照，避免关联表变化影响历史）
    quantity        INT                 NOT NULL DEFAULT 1,         -- 数量
    unit_price      DECIMAL(10, 2)      NOT NULL,                   -- 单价（快照）
    subtotal        DECIMAL(10, 2)      NOT NULL,                   -- 小计 = quantity * unit_price

    CONSTRAINT ck_order_item_type CHECK (item_type IN ('SEAT', 'SNACK')),
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_item_prices CHECK (unit_price >= 0 AND subtotal >= 0)
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);

COMMENT ON TABLE order_item IS '订单明细表：记录订单中每个座位和每款零食';
COMMENT ON COLUMN order_item.item_type IS '项目类型：SEAT-座位票，SNACK-零食';
COMMENT ON COLUMN order_item.item_id IS '对应 seat_booking.id 或 snack.id';
COMMENT ON COLUMN order_item.item_name IS '名称快照，历史订单不受后续名称变更影响';


-- =============================================================
-- 9. 支付记录表
--    记录每笔支付的流水信息
-- =============================================================
CREATE TABLE IF NOT EXISTS payment (
    id              BIGSERIAL           PRIMARY KEY,
    order_id        BIGINT              NOT NULL,                   -- 关联订单ID（业务关联）
    payment_no      VARCHAR(32)         NOT NULL,                   -- 支付流水号
    amount          DECIMAL(10, 2)      NOT NULL,                   -- 支付金额
    payment_method  VARCHAR(20),                                    -- 支付方式: WECHAT / ALIPAY / CREDIT_CARD / BALANCE
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING', -- 状态: PENDING / SUCCESS / FAILED / REFUNDED
    paid_at         TIMESTAMP,                                      -- 支付完成时间
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_payment_no UNIQUE (payment_no),
    CONSTRAINT ck_payment_amount CHECK (amount > 0),
    CONSTRAINT ck_payment_status CHECK (
        status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')
    ),
    CONSTRAINT ck_payment_method CHECK (
        payment_method IN ('WECHAT', 'ALIPAY', 'CREDIT_CARD', 'BALANCE')
    )
);

CREATE INDEX idx_payment_order_id ON payment (order_id);

COMMENT ON TABLE payment IS '支付记录表：每笔订单的支付流水';
COMMENT ON COLUMN payment.payment_method IS '支付方式：WECHAT-微信，ALIPAY-支付宝，CREDIT_CARD-信用卡，BALANCE-余额';
COMMENT ON COLUMN payment.status IS '支付状态：PENDING-处理中，SUCCESS-成功，FAILED-失败，REFUNDED-已退款';


-- =============================================================
-- 10. 零食表
--    影院可售卖的零食饮品
-- =============================================================
CREATE TABLE IF NOT EXISTS snack (
    id              BIGSERIAL           PRIMARY KEY,
    name            VARCHAR(50)         NOT NULL,                   -- 零食名称
    price           DECIMAL(10, 2)      NOT NULL,                   -- 单价
    image_url       VARCHAR(255),                                   -- 图片 URL
    stock           INT                 NOT NULL DEFAULT 0,         -- 当前库存
    status          VARCHAR(10)         NOT NULL DEFAULT 'ON',      -- 状态: ON / OFF

    CONSTRAINT ck_snack_price CHECK (price > 0),
    CONSTRAINT ck_snack_stock CHECK (stock >= 0),
    CONSTRAINT ck_snack_status CHECK (status IN ('ON', 'OFF'))
);

CREATE INDEX idx_snack_status ON snack (status);

COMMENT ON TABLE snack IS '零食表：影院可售卖的零食饮品';
COMMENT ON COLUMN snack.price IS '单价，单位：元';
COMMENT ON COLUMN snack.stock IS '当前库存数量，下单时扣减';


-- =============================================================
-- 11. 定价规则表
--    动态定价/折扣规则配置（管理端可配置）
-- =============================================================
CREATE TABLE IF NOT EXISTS pricing_rule (
    id              BIGSERIAL           PRIMARY KEY,
    rule_name       VARCHAR(100)        NOT NULL,                   -- 规则名称（如 "周二半价"）
    rule_type       VARCHAR(50)         NOT NULL,                   -- 规则类型标识（代码中用）
    rule_value      DECIMAL(10, 4)      NOT NULL,                   -- 规则值（折扣率/加价率）
    priority        INT                 NOT NULL DEFAULT 0,         -- 应用优先级（越大越优先）
    enabled         BOOLEAN             NOT NULL DEFAULT TRUE,      -- 是否启用
    description     VARCHAR(255),                                   -- 规则说明
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_pricing_rule_value CHECK (rule_value > 0)
);

CREATE INDEX idx_pricing_rule_type ON pricing_rule (rule_type);
CREATE INDEX idx_pricing_rule_enabled ON pricing_rule (enabled);

COMMENT ON TABLE pricing_rule IS '定价规则表：动态定价和折扣规则配置';
COMMENT ON COLUMN pricing_rule.rule_type IS '规则类型标识，如: MEMBER_DISCOUNT, TUESDAY_DISCOUNT, PEAK_SURCHARGE, HOLIDAY_DISCOUNT, SOLD_OUT_SURCHARGE';
COMMENT ON COLUMN pricing_rule.rule_value IS '规则值：折扣率（0.8=打8折）或加价率（1.15=加价15%）';
COMMENT ON COLUMN pricing_rule.priority IS '优先级，数值越大越先应用';
