# Cinema System - 电影院购票系统

Vue 3 + Spring Boot 3.2 全栈电影院在线购票系统。

## 环境要求

| 工具 | 版本 |
|------|------|
| Java | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.6+（或使用项目内 `mvnw`） |

## 快速启动

### 1. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS cinema
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

# 退出后执行脚本
mysql -u root -p cinema < db/schema.sql
mysql -u root -p cinema < db/seed.sql
```

### 2. 启动后端

```bash
cd cinema_backend

# 确保 application.yml 中的数据库连接配置正确
# 默认: root / root, jdbc:mysql://localhost:3306/cinema

# 使用 Maven Wrapper（推荐）
./mvnw spring-boot:run

# 或使用系统 Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端启动后：
- 服务地址：`http://localhost:8080`
- API 文档：`http://localhost:8080/swagger-ui.html`
- 健康检查：`http://localhost:8080/actuator/health`

### 3. 启动前端

```bash
cd cinema_web

# 安装依赖（首次启动或依赖变更时）
npm install

# 开发模式启动
npm run dev
```

前端启动后：
- 地址：`http://localhost:3000`
- API 已通过 Vite 代理至 `http://localhost:8080`

### 4. 打开浏览器

访问 `http://localhost:3000`

## 默认账号

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 管理员 | `admin` | `123456` | 可访问全部管理页面 |
| 会员用户 | `alice` | `123456` | 购票享 8 折 |
| 普通用户 | `bob` | `123456` | 非会员无折扣 |
| 会员用户 | `charlie` | `123456` | — |
| 普通用户 | `diana` | `123456` | — |
| 会员用户 | `eve` | `123456` | — |

## 项目结构

```
cinema_system/
├── db/                     # 数据库脚本
│   ├── schema.sql          # 建表语句（11 张表）
│   └── seed.sql            # 种子数据
├── cinema_backend/         # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../system/ # 76 个 Java 文件
│       └── resources/       # 配置 + SQL
├── cinema_web/             # Vue 3 前端
│   ├── package.json
│   └── src/
│       ├── api/            # 11 个 API 模块
│       ├── views/          # 22 个页面
│       └── router/         # 路由配置
├── start.sh                # 一键启动脚本（可选）
└── README.md
```

## 管理后台

管理员登录后可访问 `/admin` 路径下的管理页面：

- **Dashboard** - 数据统计看板
- **电影管理** - 增删改查电影
- **排片管理** - 管理放映场次
- **影厅管理** - 管理影厅与座位布局
- **定价规则** - 配置折扣/加价规则
- **用户管理** - 用户列表与状态管理
- **订单管理** - 全部订单查看

## 常用命令

```bash
# 后端 - 打包
cd cinema_backend && ./mvnw clean package -DskipTests

# 后端 - 运行 JAR
java -jar cinema_backend/target/cinema-system-*.jar

# 前端 - 构建生产版本
cd cinema_web && npm run build

# 前端 - 预览生产构建
cd cinema_web && npm run preview
```
