# 电影院购票系统 - 架构设计方案

## 技术选型

| 层次 | 技术 | 说明 |
|------|------|------|
| 前端框架 | Vue 3 + TypeScript | 组合式 API、类型安全 |
| 前端 UI | Element Plus | 企业级组件库 |
| 状态管理 | Pinia | Vue3 官方推荐 |
| 路由 | Vue Router 4 | SPA 路由 |
| 后端框架 | Spring Boot 3.2+ | Java 企业级框架 |
| ORM | Spring Data JPA + Hibernate | 对象关系映射 |
| 数据库 | PostgreSQL / MySQL | 关系型数据库 |
| 认证 | JWT (jjwt) | 无状态 Token 认证 |
| 密码加密 | BCrypt | 安全哈希 |
| API 文档 | SpringDoc OpenAPI (Swagger) | 接口文档自动生成 |
| 构建工具 | Maven | 后端构建 |
| 构建工具 | Vite | 前端构建 |

---

## 一、项目目录结构

### 后端 (`cinema_backend/`)

```
cinema_backend/
├── pom.xml
├── Dockerfile
├── src/main/java/com/cinema/system/
│   ├── CinemaApplication.java
│   │
│   ├── common/                          # 公共模块
│   │   ├── config/
│   │   │   ├── CorsConfig.java          # 跨域配置
│   │   │   ├── SecurityConfig.java      # 安全配置
│   │   │   └── SwaggerConfig.java       # Swagger 配置
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   ├── BusinessException.java       # 业务异常
│   │   │   └── UnauthorizedException.java   # 未授权异常
│   │   ├── response/
│   │   │   ├── ApiResponse.java         # 统一响应格式
│   │   │   └── PageResponse.java        # 分页响应
│   │   ├── enums/
│   │   │   ├── OrderStatusEnum.java
│   │   │   ├── SeatStatusEnum.java
│   │   │   ├── UserRoleEnum.java
│   │   │   ├── ShowingStatusEnum.java
│   │   │   └── PaymentStatusEnum.java
│   │   └── utils/
│   │       └── DateTimeUtil.java
│   │
│   ├── auth/                            # 认证授权模块
│   │   ├── config/JwtConfig.java
│   │   ├── filter/JwtAuthenticationFilter.java
│   │   ├── controller/AuthController.java
│   │   ├── service/AuthService.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   └── RegisterRequest.java
│   │   └── util/JwtUtil.java
│   │
│   ├── user/                            # 用户模块
│   │   ├── controller/UserController.java
│   │   ├── service/UserService.java
│   │   ├── repository/UserRepository.java
│   │   ├── entity/User.java
│   │   └── dto/
│   │       ├── UserRegisterRequest.java
│   │       ├── UserLoginRequest.java
│   │       └── UserProfileResponse.java
│   │
│   ├── movie/                           # 电影模块
│   │   ├── controller/MovieController.java
│   │   ├── service/MovieService.java
│   │   ├── repository/MovieRepository.java
│   │   ├── entity/Movie.java
│   │   └── dto/
│   │       ├── MovieCreateRequest.java
│   │       ├── MovieUpdateRequest.java
│   │       └── MovieListResponse.java
│   │
│   ├── hall/                            # 影厅模块
│   │   ├── controller/HallController.java
│   │   ├── service/HallService.java
│   │   ├── repository/HallRepository.java
│   │   ├── repository/HallSeatRepository.java
│   │   ├── entity/Hall.java
│   │   ├── entity/HallSeat.java
│   │   └── dto/
│   │       └── HallCreateRequest.java
│   │
│   ├── showing/                         # 排片模块
│   │   ├── controller/ShowingController.java
│   │   ├── service/ShowingService.java
│   │   ├── repository/ShowingRepository.java
│   │   ├── entity/Showing.java
│   │   └── dto/
│   │       ├── ShowingCreateRequest.java
│   │       └── ShowingListResponse.java
│   │
│   ├── seat/                            # 座位模块
│   │   ├── controller/SeatController.java
│   │   ├── service/SeatService.java
│   │   ├── repository/SeatBookingRepository.java
│   │   ├── entity/SeatBooking.java
│   │   └── dto/
│   │       └── SeatBookingResponse.java
│   │
│   ├── snack/                           # 零食模块
│   │   ├── controller/SnackController.java
│   │   ├── service/SnackService.java
│   │   ├── repository/SnackRepository.java
│   │   ├── entity/Snack.java
│   │   └── dto/
│   │       └── SnackCreateRequest.java
│   │
│   ├── order/                           # 订单模块
│   │   ├── controller/OrderController.java
│   │   ├── service/OrderService.java
│   │   ├── repository/OrderRepository.java
│   │   ├── repository/OrderItemRepository.java
│   │   ├── entity/Order.java
│   │   ├── entity/OrderItem.java
│   │   └── dto/
│   │       ├── OrderCreateRequest.java
│   │       ├── OrderListResponse.java
│   │       └── OrderDetailResponse.java
│   │
│   ├── payment/                         # 支付模块
│   │   ├── controller/PaymentController.java
│   │   ├── service/PaymentService.java
│   │   ├── repository/PaymentRepository.java
│   │   ├── entity/Payment.java
│   │   └── dto/
│   │       └── PaymentRequest.java
│   │
│   └── pricing/                         # 定价模块
│       ├── controller/PricingRuleController.java
│       ├── service/PricingRuleService.java
│       ├── repository/PricingRuleRepository.java
│       ├── entity/PricingRule.java
│       └── dto/
│           └── PricingRuleUpdateRequest.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/
│       └── schema.sql                   # 数据库初始化脚本
│
└── src/test/java/com/cinema/system/
    └── ...                              # 单元测试
```

### 前端 (`cinema_web/`)

```
cinema_web/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── .env.development
├── .env.production
├── Dockerfile
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── main.ts                          # 入口文件
    ├── App.vue                          # 根组件
    ├── router/
    │   └── index.ts                     # 路由配置
    │
    ├── stores/                          # Pinia 状态管理
    │   ├── user.ts                      # 用户状态
    │   └── app.ts                       # 应用全局状态
    │
    ├── api/                             # API 接口层
    │   ├── request.ts                   # Axios 封装
    │   ├── auth.ts                      # 认证相关 API
    │   ├── user.ts                      # 用户相关 API
    │   ├── movie.ts                     # 电影相关 API
    │   ├── showing.ts                   # 排片相关 API
    │   ├── hall.ts                      # 影厅相关 API
    │   ├── seat.ts                      # 座位相关 API
    │   ├── snack.ts                     # 零食相关 API
    │   ├── order.ts                     # 订单相关 API
    │   └── pricing.ts                   # 定价规则 API
    │
    ├── views/                           # 页面组件
    │   ├── user/
    │   │   ├── LoginPage.vue            # 登录页
    │   │   ├── RegisterPage.vue         # 注册页
    │   │   └── ProfilePage.vue          # 个人中心
    │   │
    │   ├── movie/
    │   │   ├── MovieListPage.vue        # 电影列表页
    │   │   └── MovieDetailPage.vue      # 电影详情页
    │   │
    │   ├── booking/
    │   │   ├── ShowingPage.vue          # 排片选择页
    │   │   ├── SeatSelectPage.vue       # 选座页
    │   │   └── SnackOrderPage.vue       # 零食页
    │   │
    │   ├── order/
    │   │   ├── OrderConfirmPage.vue     # 订单确认页
    │   │   ├── PaymentPage.vue          # 支付页
    │   │   ├── PaymentSuccessPage.vue    # 支付成功页
    │   │   ├── OrderListPage.vue        # 订单列表页
    │   │   └── OrderDetailPage.vue      # 订单详情页
    │   │
    │   └── admin/
    │       ├── DashboardPage.vue        # 管理首页（数据统计）
    │       ├── MovieManagePage.vue      # 电影管理
    │       ├── ShowingManagePage.vue    # 排片管理
    │       ├── HallManagePage.vue       # 影厅管理
    │       ├── UserManagePage.vue       # 用户管理
    │       ├── PricingRulePage.vue      # 定价规则管理
    │       └── OrderManagePage.vue      # 订单管理
    │
    ├── components/                      # 通用组件
    │   ├── AppHeader.vue                # 顶部导航栏
    │   ├── AppSidebar.vue               # 侧边栏
    │   ├── MovieCard.vue                # 电影卡片
    │   ├── SeatGrid.vue                 # 座位网格组件
    │   └── PriceSummary.vue             # 价格汇总组件
    │
    ├── composables/                     # 组合式函数
    │   ├── useAuth.ts                   # 认证相关
    │   └── usePricing.ts               # 定价计算
    │
    └── styles/
        ├── variables.scss               # 全局变量
        └── global.scss                  # 全局样式
```

---

## 二、后端分层架构

每层模块遵循严格的 **Controller → Service → Repository** 三层架构：

```
┌─────────────────────────────────────────────────────────┐
│                   Controller 层                          │
│  接收请求参数 → 调用 Service → 返回 ApiResponse          │
│  职责：参数校验、路由映射、统一响应                        │
├─────────────────────────────────────────────────────────┤
│                    Service 层                            │
│  业务逻辑编排 → 调用 Repository → 事务管理                │
│  职责：核心业务逻辑、事务控制、DTO 转换                    │
├─────────────────────────────────────────────────────────┤
│                   Repository 层                          │
│  JPA 数据访问 → 数据库交互                               │
│  职责：数据 CRUD、复杂查询                               │
├─────────────────────────────────────────────────────────┤
│                     Entity 层                            │
│  JPA 实体映射 → 数据库表对应                             │
├─────────────────────────────────────────────────────────┤
│                    数据库 (PostgreSQL)                   │
└─────────────────────────────────────────────────────────┘
```

### 统一响应格式

```java
public class ApiResponse<T> {
    private int code;        // 200 成功, 4xx 客户端错误, 5xx 服务端错误
    private String message;  // 提示信息
    private T data;          // 响应数据
    private long timestamp;  // 时间戳
}
```

### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // BusinessException → 400
    // UnauthorizedException → 401
    // ValidationException → 422
    // Exception → 500
}
```

---

## 三、数据库 ER 关系图

```
user (1) ──── (N) orders (N) ──── (1) showing (N) ──── (1) movie
                │                                        │
                │                                  (N)   │
                │                                        │
                ├── (N) order_item ──── seat_booking ────┤
                │                                        │
                ├── (1) payment                          │
                │                                        │
                └── (N) order_item ──── snack            │
                                                        │
hall (1) ──── (N) hall_seat                             │
                │                                       │
                └── (N) seat_booking ────────────────────┘

pricing_rule (独立表，由管理员配置)
```

---

## 四、核心业务流程时序

### 4.1 用户注册

```
前端 RegisterPage
  → POST /api/auth/register
  → AuthController.register(RegisterRequest)
  → AuthService.register()
    → 校验参数 (用户名、密码、手机号等)
    → 检查 userId 是否重复
    → BCrypt 加密密码
    → UserRepository.save(User)
  → 返回 ApiResponse.success("注册成功")
```

### 4.2 用户登录

```
前端 LoginPage
  → POST /api/auth/login
  → AuthController.login(LoginRequest)
  → AuthService.login()
    → UserRepository.findByUserId()
    → BCrypt 密码校验
    → 生成 JWT Token (含 userId, role, 过期时间)
  → 返回 LoginResponse { token, userInfo }
  → 前端将 Token 存入 Pinia + localStorage
  → 后续请求在 Header 携带 Authorization: Bearer <token>
```

### 4.3 选座购票（核心流程）

```
1. 选择电影 → 获取排片
   前端 → GET /api/showings?movieId={id}&date={date}
   → ShowingController → ShowingService → ShowingRepository

2. 选择场次 → 加载座位
   前端 → GET /api/showings/{showingId}/seats
   → SeatController → SeatService → SeatBookingRepository
   → 返回所有座位及其状态 (AVAILABLE/LOCKED/BOOKED)

3. 锁定座位
   前端 → POST /api/seats/lock
   → SeatController → SeatService.lockSeats(showingId, seatIds, userId)
     → 检查座位是否 AVAILABLE
     → 批量更新状态为 LOCKED
     → 记录锁定时间 (用于超时释放)
   → 返回锁定成功

4. 选购零食 (可选)
   前端浏览零食列表 → 加入购物车（前端本地）

5. 创建订单
   前端 → POST /api/orders
   → OrderController → OrderService.createOrder(OrderCreateRequest)
     → 计算动态价格 (调用 PricingRuleService)
     → 验证座位仍为 LOCKED 状态
     → 批量更新座位为 BOOKED
     → 创建 Order + OrderItem (座位 + 零食)
     → 扣减零食库存
   → 返回订单信息

6. 支付
   前端 → POST /api/payments/pay
   → PaymentController → PaymentService.processPayment()
     → 创建支付记录
     → 更新订单状态为 PAID
   → 返回支付成功

7. 超时自动释放（后台定时任务）
   @Scheduled 每分钟执行
   → 查找锁定超过 15 分钟的座位
   → 自动释放为 AVAILABLE
```

### 4.4 退票流程

```
前端 → POST /api/orders/{orderId}/refund
→ OrderController → OrderService.refundOrder(orderId)
  → 校验订单状态为 PAID
  → 更新座位状态为 AVAILABLE
  → 更新订单状态为 REFUNDED
  → 创建退款支付记录
→ 返回退票成功
```

---

## 五、API 接口总览

### 认证模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |

### 用户模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/users/me` | 获取当前用户信息 | 是 |
| PUT | `/api/users/me` | 更新个人信息 | 是 |

### 电影模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/movies` | 电影列表(分页+搜索+筛选) | 否 |
| GET | `/api/movies/{id}` | 电影详情 | 否 |
| POST | `/api/movies` | 新增电影 | 是(Admin) |
| PUT | `/api/movies/{id}` | 更新电影 | 是(Admin) |
| DELETE | `/api/movies/{id}` | 删除电影 | 是(Admin) |

### 影厅模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/halls` | 影厅列表 | 是 |
| GET | `/api/halls/{id}` | 影厅详情(含座位布局) | 是 |
| POST | `/api/halls` | 新增影厅 | 是(Admin) |

### 排片模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/showings` | 排片列表(按电影/日期筛选) | 否 |
| GET | `/api/showings/{id}` | 排片详情 | 否 |
| POST | `/api/showings` | 新增排片 | 是(Admin) |
| DELETE | `/api/showings/{id}` | 取消排片 | 是(Admin) |

### 座位模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/showings/{showingId}/seats` | 获取场次座位状态 | 是 |
| POST | `/api/seats/lock` | 锁定座位 | 是 |
| POST | `/api/seats/unlock` | 释放座位 | 是 |

### 零食模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/snacks` | 零食列表 | 否 |
| POST | `/api/snacks` | 新增零食 | 是(Admin) |
| PUT | `/api/snacks/{id}` | 更新零食 | 是(Admin) |

### 订单模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/orders` | 创建订单 | 是 |
| GET | `/api/orders` | 我的订单列表 | 是 |
| GET | `/api/orders/{id}` | 订单详情 | 是 |
| POST | `/api/orders/{id}/refund` | 申请退票 | 是 |

### 支付模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/payments/pay` | 支付 | 是 |

### 定价规则模块
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/pricing-rules` | 定价规则列表 | 是(Admin) |
| PUT | `/api/pricing-rules/{id}` | 更新规则 | 是(Admin) |

### 管理端 - 用户管理
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/admin/users` | 用户列表(分页) | 是(Admin) |
| PUT | `/api/admin/users/{id}` | 更新用户(会员/禁用) | 是(Admin) |

### 管理端 - 数据统计
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/admin/dashboard` | Dashboard 统计数据 | 是(Admin) |

---

## 六、数据库完整设计

请参考上节已列出的 9 张核心表：
`user`, `movie`, `hall`, `hall_seat`, `showing`, `seat_booking`, `orders`, `order_item`, `payment`, `snack`, `pricing_rule`

---

## 七、安全设计

1. **JWT 认证**
   - 登录成功后颁发 Token，有效期 24 小时
   - 前端请求在 `Authorization` Header 携带
   - JWT 过滤器拦截非公开接口，解析 Token 并注入用户信息

2. **密码安全**
   - 使用 BCrypt 加密存储，不可逆
   - 登录时 BCrypt 验证

3. **接口权限控制**
   - 角色枚举：`USER`, `ADMIN`
   - 管理端接口校验 `ADMIN` 角色
   - 用户端接口校验 `USER` 或 `ADMIN`

4. **跨域配置**
   - CORS 允许前端域名
   - 生产环境限定具体域名

---

## 八、环境配置

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cinema
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # 生产用 validate，开发用 update
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24小时

server:
  port: 8080
```

---

## 九、实施计划

| 阶段 | 内容 | 预计步骤 |
|------|------|----------|
| **Phase 1** | 搭建项目骨架、公共模块、JWT 认证 | 5 步 |
| **Phase 2** | 用户 + 电影 + 影厅 + 排片模块 | 4 步 |
| **Phase 3** | 座位 + 零食 + 定价规则模块 | 3 步 |
| **Phase 4** | 订单 + 支付模块（核心业务流） | 3 步 |
| **Phase 5** | 管理端 API（Dashboard / 用户管理） | 2 步 |
| **Phase 6** | 前端项目搭建 + 页面开发 | 10+ 步 |
| **Phase 7** | 联调测试 + 部署 | 3 步 |
