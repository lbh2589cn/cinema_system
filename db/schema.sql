-- =============================================================
-- 1. Users
--    Stores system user information, including users and admins
-- =============================================================
CREATE TABLE IF NOT EXISTS `user` (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    user_id         VARCHAR(50)         NOT NULL COMMENT 'Login account (unique)',
    username        VARCHAR(50)         NOT NULL COMMENT 'Display name',
    password_hash   VARCHAR(255)        NOT NULL COMMENT 'BCrypt encrypted password',
    phone           VARCHAR(20)         COMMENT 'Phone number',
    role            VARCHAR(10)         NOT NULL DEFAULT 'USER' COMMENT 'Role: USER / ADMIN',
    is_member       BOOLEAN             NOT NULL DEFAULT FALSE COMMENT 'Is member',
    status          VARCHAR(10)         NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status: ACTIVE / DISABLED',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_user_id UNIQUE (user_id),
    CONSTRAINT ck_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_user_phone ON `user` (phone);
CREATE INDEX idx_user_status ON `user` (status);


-- =============================================================
-- 2. Movies
--    Stores movie metadata (title, poster, description, rating, etc.)
-- =============================================================
CREATE TABLE IF NOT EXISTS movie (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    title           VARCHAR(100)        NOT NULL COMMENT 'Movie title',
    poster_url      VARCHAR(255)        COMMENT 'Poster image URL',
    description     TEXT                COMMENT 'Synopsis',
    duration        INT                 NOT NULL COMMENT 'Duration (minutes)',
    rating          DECIMAL(3,1)        DEFAULT 0.0 COMMENT 'Rating (0.0-10.0)',
    genre           VARCHAR(100)        COMMENT 'Genre (comma-separated)',
    release_date    DATE                COMMENT 'Release date',
    status          VARCHAR(10)         NOT NULL DEFAULT 'ON' COMMENT 'Status: ON / OFF',
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
-- 3. Halls
--    Defines cinema hall basic info (name, seat layout)
-- =============================================================
CREATE TABLE IF NOT EXISTS hall (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    name            VARCHAR(50)         NOT NULL COMMENT 'Hall name',
    `rows`          INT                 NOT NULL COMMENT 'Number of seat rows',
    `cols`          INT                 NOT NULL COMMENT 'Number of seat columns',
    seat_count      INT                 NOT NULL COMMENT 'Total seat count',
    description     VARCHAR(255)        COMMENT 'Hall description',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_hall_rows CHECK (`rows` > 0),
    CONSTRAINT ck_hall_cols CHECK (`cols` > 0),
    CONSTRAINT ck_hall_seat_count CHECK (seat_count > 0)
);


-- =============================================================
-- 4. Seats
--    Fixed seat layout per hall
-- =============================================================
CREATE TABLE IF NOT EXISTS hall_seat (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    hall_id         BIGINT              NOT NULL COMMENT 'Associated hall',
    row_num         INT                 NOT NULL COMMENT 'Row number (starting from 1)',
    col_num         INT                 NOT NULL COMMENT 'Column number (starting from 1)',
    status          VARCHAR(20)         NOT NULL DEFAULT 'STANDARD' COMMENT 'Seat status',
    CONSTRAINT uk_hall_seat UNIQUE (hall_id, row_num, col_num),
    CONSTRAINT ck_hall_seat_status CHECK (status IN ('STANDARD', 'VIP', 'UNAVAILABLE')),
    CONSTRAINT ck_hall_seat_row CHECK (row_num > 0),
    CONSTRAINT ck_hall_seat_col CHECK (col_num > 0)
);

CREATE INDEX idx_hall_seat_hall_id ON hall_seat (hall_id);


-- =============================================================
-- 5. Showings
--    Movie screenings in specific halls
-- =============================================================
CREATE TABLE IF NOT EXISTS showing (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    movie_id        BIGINT              NOT NULL COMMENT 'Movie ID',
    hall_id         BIGINT              NOT NULL COMMENT 'Hall ID',
    show_date       DATE                NOT NULL COMMENT 'Show date',
    show_time       TIME                NOT NULL COMMENT 'Show time',
    base_price      DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT 'Base ticket price',
    status          VARCHAR(20)         NOT NULL DEFAULT 'SCHEDULED' COMMENT 'Status',
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
-- 6. Seat Bookings
--    Booking state of each seat per showing
-- =============================================================
CREATE TABLE IF NOT EXISTS seat_booking (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    showing_id      BIGINT              NOT NULL COMMENT 'Showing ID',
    seat_id         BIGINT              NOT NULL COMMENT 'Seat ID',
    status          VARCHAR(20)         NOT NULL DEFAULT 'AVAILABLE' COMMENT 'Status',
    locked_by       BIGINT              COMMENT 'Locked by (user ID)',
    locked_at       TIMESTAMP           COMMENT 'Lock time',
    booked_by       BIGINT              COMMENT 'Booked by (user ID)',
    booked_at       TIMESTAMP           COMMENT 'Booking time',
    order_id        BIGINT              COMMENT 'Associated order ID',
    version         INT                 NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',

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
-- 7. Orders
--    User ticket purchase order master table
-- =============================================================
CREATE TABLE IF NOT EXISTS `order` (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    order_no        VARCHAR(32)         NOT NULL COMMENT 'Order number',
    user_id         BIGINT              NOT NULL COMMENT 'User ID',
    showing_id      BIGINT              NOT NULL COMMENT 'Showing ID',
    seat_count      INT                 NOT NULL DEFAULT 0 COMMENT 'Number of seats',
    total_amount    DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT 'Total amount',
    discount_amount DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT 'Discount amount',
    final_amount    DECIMAL(10, 2)      NOT NULL DEFAULT 0.00 COMMENT 'Final amount',
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING' COMMENT 'Status',
    remark          VARCHAR(255)        COMMENT 'Remark',
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
-- 8. Order Items
--    Stores each item in an order (seats and snacks)
-- =============================================================
CREATE TABLE IF NOT EXISTS order_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    order_id        BIGINT              NOT NULL COMMENT 'Associated order ID',
    item_type       VARCHAR(10)         NOT NULL COMMENT 'Item type: SEAT / SNACK',
    item_id         BIGINT              NOT NULL COMMENT 'Item ID',
    item_name       VARCHAR(100)        NOT NULL COMMENT 'Item name',
    quantity        INT                 NOT NULL DEFAULT 1 COMMENT 'Quantity',
    unit_price      DECIMAL(10, 2)      NOT NULL COMMENT 'Unit price',
    subtotal        DECIMAL(10, 2)      NOT NULL COMMENT 'Subtotal',

    CONSTRAINT ck_order_item_type CHECK (item_type IN ('SEAT', 'SNACK')),
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_item_prices CHECK (unit_price >= 0 AND subtotal >= 0)
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);


-- =============================================================
-- 9. Payment Records
--    Records payment transaction information
-- =============================================================
CREATE TABLE IF NOT EXISTS payment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    order_id        BIGINT              NOT NULL COMMENT 'Associated order ID',
    payment_no      VARCHAR(32)         NOT NULL COMMENT 'Payment transaction number',
    amount          DECIMAL(10, 2)      NOT NULL COMMENT 'Payment amount',
    payment_method  VARCHAR(20)         COMMENT 'Payment method',
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING' COMMENT 'Status',
    paid_at         TIMESTAMP           COMMENT 'Payment completion time',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_payment_no UNIQUE (payment_no),
    CONSTRAINT ck_payment_amount CHECK (amount > 0),
    CONSTRAINT ck_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    CONSTRAINT ck_payment_method CHECK (payment_method IN ('WECHAT', 'ALIPAY', 'CREDIT_CARD', 'BALANCE'))
);

CREATE INDEX idx_payment_order_id ON payment (order_id);


-- =============================================================
-- 10. Snacks
--     Cinema snacks and beverages for sale
-- =============================================================
CREATE TABLE IF NOT EXISTS snack (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    name            VARCHAR(50)         NOT NULL COMMENT 'Snack name',
    price           DECIMAL(10, 2)      NOT NULL COMMENT 'Unit price',
    image_url       VARCHAR(255)        COMMENT 'Image URL',
    stock           INT                 NOT NULL DEFAULT 0 COMMENT 'Current stock',
    status          VARCHAR(10)         NOT NULL DEFAULT 'ON' COMMENT 'Status: ON / OFF',

    CONSTRAINT ck_snack_price CHECK (price > 0),
    CONSTRAINT ck_snack_stock CHECK (stock >= 0),
    CONSTRAINT ck_snack_status CHECK (status IN ('ON', 'OFF'))
);

CREATE INDEX idx_snack_status ON snack (status);


-- =============================================================
-- 11. Pricing Rules
--     Dynamic pricing / discount rule configuration
-- =============================================================
CREATE TABLE IF NOT EXISTS pricing_rule (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    rule_name       VARCHAR(100)        NOT NULL COMMENT 'Rule name',
    rule_value      DECIMAL(10, 4)      NOT NULL COMMENT 'Rule value (discount/surcharge rate)',
    priority        INT                 NOT NULL DEFAULT 0 COMMENT 'Application priority',
    enabled         BOOLEAN             NOT NULL DEFAULT TRUE COMMENT 'Is enabled',
    description     VARCHAR(255)        COMMENT 'Rule description',

    condition_member      BOOLEAN       COMMENT 'Condition: is member',
    condition_weekdays    VARCHAR(50)   COMMENT 'Condition: day of week',
    condition_time_start  TIME          COMMENT 'Condition: time range start',
    condition_time_end    TIME          COMMENT 'Condition: time range end',
    condition_ticket_min  INT           COMMENT 'Condition: min ticket count',
    condition_ticket_max  INT           COMMENT 'Condition: max ticket count',
    condition_seat_ratio_min  DECIMAL(5,2) COMMENT 'Condition: min remaining seat ratio',
    condition_seat_ratio_max  DECIMAL(5,2) COMMENT 'Condition: max remaining seat ratio',

    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT ck_pricing_rule_value CHECK (rule_value > 0),
    CONSTRAINT ck_pricing_rule_seat_ratio CHECK (
        (condition_seat_ratio_min IS NULL OR (condition_seat_ratio_min >= 0 AND condition_seat_ratio_min <= 1))
        AND (condition_seat_ratio_max IS NULL OR (condition_seat_ratio_max >= 0 AND condition_seat_ratio_max <= 1))
    )
);

CREATE INDEX idx_pricing_rule_enabled ON pricing_rule (enabled);
