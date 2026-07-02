#!/bin/bash
# =============================================================
# 电影院购票系统 - 本地一键启动脚本
# 用法: ./start.sh [选项]
#   选项:
#     -b, --backend-only     仅启动后端
#     -f, --frontend-only    仅启动前端
#     -d, --db-only          仅初始化数据库
#     -h, --help             显示帮助信息
# =============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/cinema_backend"
FRONTEND_DIR="$PROJECT_DIR/cinema_web"
DB_DIR="$PROJECT_DIR/db"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

info()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()    { echo -e "${GREEN}[OK]${NC}    $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
err()   { echo -e "${RED}[ERROR]${NC} $1"; }

# =============================================================
# 环境检查
# =============================================================
check_prerequisites() {
    info "检查运行环境..."

    # Java
    if command -v java &>/dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VER" -ge 17 ] 2>/dev/null; then
            ok "Java $(java -version 2>&1 | head -1)"
        else
            warn "Java 版本低于 17，当前: $(java -version 2>&1 | head -1)"
            warn "建议使用 Java 17+"
        fi
    else
        err "未检测到 Java，请安装 JDK 17+"
        exit 1
    fi

    # Maven Wrapper
    if [ ! -f "$BACKEND_DIR/mvnw" ]; then
        info "生成 Maven Wrapper..."
        if command -v mvn &>/dev/null; then
            cd "$BACKEND_DIR" && mvn wrapper:wrapper -Dmaven=3.9.6 2>/dev/null || true
        fi
        if [ ! -f "$BACKEND_DIR/mvnw" ]; then
            warn "未找到 mvnw，将尝试使用系统 mvn 命令"
        fi
    fi

    # Node.js
    if command -v node &>/dev/null; then
        ok "Node.js $(node --version)"
    else
        err "未检测到 Node.js，请安装 Node.js 18+"
        exit 1
    fi

    # npm
    if command -v npm &>/dev/null; then
        ok "npm $(npm --version)"
    else
        err "未检测到 npm"
        exit 1
    fi

    # MySQL
    if command -v mysql &>/dev/null; then
        ok "MySQL $(mysql --version 2>&1 | head -1)"
    else
        warn "未检测到 mysql 命令，请确保 MySQL 8.0+ 已安装并运行"
        warn "数据库连接配置见: $BACKEND_DIR/src/main/resources/application.yml"
    fi
}

# =============================================================
# 数据库初始化
# =============================================================
init_database() {
    info "初始化数据库..."

    if ! command -v mysql &>/dev/null; then
        warn "mysql 不可用，跳过数据库初始化"
        warn "请手动执行以下 SQL 脚本:"
        echo "  1. $DB_DIR/schema.sql"
        echo "  2. $DB_DIR/seed.sql"
        return
    fi

    # 数据库配置（与 application.yml 保持一致）
    DB_NAME="${CINEMA_DB_NAME:-cinema}"
    DB_USER="${CINEMA_DB_USER:-root}"
    DB_PASS="${CINEMA_DB_PASS:-root}"
    DB_HOST="${CINEMA_DB_HOST:-localhost}"
    DB_PORT="${CINEMA_DB_PORT:-3306}"

    # 创建数据库（如不存在）
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" \
        -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    ok "数据库 $DB_NAME 已就绪"

    # 执行 schema.sql
    info "执行 schema.sql..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$DB_DIR/schema.sql"
    ok "schema.sql 执行完成"

    # 执行 seed.sql
    info "执行 seed.sql..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$DB_DIR/seed.sql"
    ok "seed.sql 执行完成"
}

# =============================================================
# 启动后端
# =============================================================
start_backend() {
    info "启动后端服务..."

    cd "$BACKEND_DIR"

    MVN_CMD=""
    if [ -f "./mvnw" ]; then
        MVN_CMD="./mvnw"
        chmod +x ./mvnw
    elif command -v mvn &>/dev/null; then
        MVN_CMD="mvn"
    else
        err "未找到 Maven 命令"
        exit 1
    fi

    info "使用 $MVN_CMD 启动 Spring Boot..."
    info "后端将在 http://localhost:8080 启动"

    # 使用 dev 配置文件启动
    $MVN_CMD spring-boot:run -Dspring-boot.run.profiles=dev \
        > "$PROJECT_DIR/backend.log" 2>&1 &

    BACKEND_PID=$!
    echo $BACKEND_PID > "$PROJECT_DIR/.backend.pid"
    ok "后端服务已启动 (PID: $BACKEND_PID)"
    info "日志文件: $PROJECT_DIR/backend.log"
}

# =============================================================
# 启动前端
# =============================================================
start_frontend() {
    info "启动前端开发服务器..."

    cd "$FRONTEND_DIR"

    # 安装依赖（如 node_modules 不存在）
    if [ ! -d "node_modules" ]; then
        info "安装前端依赖..."
        npm install
        ok "前端依赖安装完成"
    fi

    info "前端将在 http://localhost:3000 启动"
    npx vite --host 0.0.0.0 > "$PROJECT_DIR/frontend.log" 2>&1 &

    FRONTEND_PID=$!
    echo $FRONTEND_PID > "$PROJECT_DIR/.frontend.pid"
    ok "前端服务已启动 (PID: $FRONTEND_PID)"
    info "日志文件: $PROJECT_DIR/frontend.log"
}

# =============================================================
# 停止所有服务
# =============================================================
stop_all() {
    info "停止所有服务..."

    if [ -f "$PROJECT_DIR/.backend.pid" ]; then
        PID=$(cat "$PROJECT_DIR/.backend.pid")
        kill "$PID" 2>/dev/null && ok "后端服务已停止 (PID: $PID)" || warn "后端进程 $PID 不存在"
        rm -f "$PROJECT_DIR/.backend.pid"
    fi

    if [ -f "$PROJECT_DIR/.frontend.pid" ]; then
        PID=$(cat "$PROJECT_DIR/.frontend.pid")
        kill "$PID" 2>/dev/null && ok "前端服务已停止 (PID: $PID)" || warn "前端进程 $PID 不存在"
        rm -f "$PROJECT_DIR/.frontend.pid"
    fi

    # 查找并关闭残留的 Vite / Spring Boot 进程
    lsof -ti:3000 2>/dev/null | xargs kill 2>/dev/null || true
    lsof -ti:8080 2>/dev/null | xargs kill 2>/dev/null || true

    ok "所有服务已停止"
}

# =============================================================
# 显示服务状态
# =============================================================
show_status() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  电影院购票系统 - 服务状态${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "  前端:  $(lsof -ti:3000 2>/dev/null && echo -e "${GREEN}运行中${NC}" || echo -e "${RED}未启动${NC}")  http://localhost:3000"
    echo -e "  后端:  $(lsof -ti:8080 2>/dev/null && echo -e "${GREEN}运行中${NC}" || echo -e "${RED}未启动${NC}")  http://localhost:8080"
    echo -e "  API:   http://localhost:8080/swagger-ui.html"
    echo ""
    echo -e "  默认管理员账号: admin / 123456"
    echo -e "  默认用户账号:   alice / 123456"
    echo -e "                   bob  / 123456"
    echo ""
    echo -e "  停止服务: ${CYAN}./start.sh --stop${NC}"
    echo ""
}

# =============================================================
# 主函数
# =============================================================
main() {
    # 如果没有参数，启动全部
    if [ $# -eq 0 ]; then
        check_prerequisites
        init_database
        start_backend
        start_frontend
        # 等待服务就绪
        sleep 3
        show_status
        return
    fi

    case "$1" in
        -b|--backend-only)
            check_prerequisites
            start_backend
            ;;
        -f|--frontend-only)
            check_prerequisites
            start_frontend
            ;;
        -d|--db-only)
            init_database
            ;;
        --stop)
            stop_all
            ;;
        -h|--help)
            echo "用法: ./start.sh [选项]"
            echo ""
            echo "选项:"
            echo "  -b, --backend-only     仅启动后端"
            echo "  -f, --frontend-only    仅启动前端"
            echo "  -d, --db-only          仅初始化数据库"
            echo "  --stop                 停止所有服务"
            echo "  -h, --help             显示帮助信息"
            echo ""
            echo "无参数时默认启动全部服务（数据库 + 后端 + 前端）"
            ;;
        *)
            err "未知选项: $1"
            echo "用法: ./start.sh [-b|--backend-only] [-f|--frontend-only] [-d|--db-only] [--stop] [-h|--help]"
            exit 1
            ;;
    esac
}

main "$@"
