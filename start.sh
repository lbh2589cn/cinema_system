#!/bin/bash
# =============================================================
# Cinema Booking System - One-click Local Startup Script
# Usage: ./start.sh [options]
#   Options:
#     -b, --backend-only     Start backend only
#     -f, --frontend-only    Start frontend only
#     -d, --db-only          Initialise database only
#     -h, --help             Show help information
# =============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/cinema_backend"
FRONTEND_DIR="$PROJECT_DIR/cinema_web"
DB_DIR="$PROJECT_DIR/db"

# Colour output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Colour

info()  { echo -e "${CYAN}[INFO]${NC}  $1"; }
ok()    { echo -e "${GREEN}[OK]${NC}    $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
err()   { echo -e "${RED}[ERROR]${NC} $1"; }

# =============================================================
# Environment Check
# =============================================================
check_prerequisites() {
    info "Checking runtime environment..."

    # Java
    if command -v java &>/dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VER" -ge 17 ] 2>/dev/null; then
            ok "Java $(java -version 2>&1 | head -1)"
        else
            warn "Java version is below 17, current: $(java -version 2>&1 | head -1)"
            warn "It is recommended to use Java 17+"
        fi
    else
        err "Java not detected. Please install JDK 17+"
        exit 1
    fi

    # Maven Wrapper
    if [ ! -f "$BACKEND_DIR/mvnw" ]; then
        info "Generating Maven Wrapper..."
        if command -v mvn &>/dev/null; then
            cd "$BACKEND_DIR" && mvn wrapper:wrapper -Dmaven=3.9.6 2>/dev/null || true
        fi
        if [ ! -f "$BACKEND_DIR/mvnw" ]; then
            warn "mvnw not found, will attempt to use system mvn command"
        fi
    fi

    # Node.js
    if command -v node &>/dev/null; then
        ok "Node.js $(node --version)"
    else
        err "Node.js not detected. Please install Node.js 18+"
        exit 1
    fi

    # npm
    if command -v npm &>/dev/null; then
        ok "npm $(npm --version)"
    else
        err "npm not detected"
        exit 1
    fi

    # MySQL
    if command -v mysql &>/dev/null; then
        ok "MySQL $(mysql --version 2>&1 | head -1)"
    else
        warn "mysql command not detected. Please ensure MySQL 8.0+ is installed and running"
        warn "Database connection config at: $BACKEND_DIR/src/main/resources/application.yml"
    fi
}

# =============================================================
# Database Initialisation
# =============================================================
init_database() {
    info "Initialising database..."

    if ! command -v mysql &>/dev/null; then
        warn "mysql unavailable, skipping database initialisation"
        warn "Please manually execute the following SQL scripts:"
        echo "  1. $DB_DIR/schema.sql"
        echo "  2. $DB_DIR/seed.sql"
        return
    fi

    # Database config (must match application.yml)
    DB_NAME="${CINEMA_DB_NAME:-cinema}"
    DB_USER="${CINEMA_DB_USER:-root}"
    DB_PASS="${CINEMA_DB_PASS:-root}"
    DB_HOST="${CINEMA_DB_HOST:-localhost}"
    DB_PORT="${CINEMA_DB_PORT:-3306}"

    # Create database if not exists
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" \
        -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    ok "Database $DB_NAME is ready"

    # Execute schema.sql
    info "Executing schema.sql..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$DB_DIR/schema.sql"
    ok "schema.sql executed successfully"

    # Execute seed.sql
    info "Executing seed.sql..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$DB_DIR/seed.sql"
    ok "seed.sql executed successfully"
}

# =============================================================
# Start Backend
# =============================================================
start_backend() {
    info "Starting backend service..."

    cd "$BACKEND_DIR"

    MVN_CMD=""
    if [ -f "./mvnw" ]; then
        MVN_CMD="./mvnw"
        chmod +x ./mvnw
    elif command -v mvn &>/dev/null; then
        MVN_CMD="mvn"
    else
        err "Maven command not found"
        exit 1
    fi

    info "Starting Spring Boot with $MVN_CMD..."
    info "Backend will be available at http://localhost:8080"

    # Start with dev profile
    $MVN_CMD spring-boot:run -Dspring-boot.run.profiles=dev \
        > "$PROJECT_DIR/backend.log" 2>&1 &

    BACKEND_PID=$!
    echo $BACKEND_PID > "$PROJECT_DIR/.backend.pid"
    ok "Backend service started (PID: $BACKEND_PID)"
    info "Log file: $PROJECT_DIR/backend.log"
}

# =============================================================
# Start Frontend
# =============================================================
start_frontend() {
    info "Starting frontend dev server..."

    cd "$FRONTEND_DIR"

    # Install dependencies if node_modules not present
    if [ ! -d "node_modules" ]; then
        info "Installing frontend dependencies..."
        npm install
        ok "Frontend dependencies installed"
    fi

    info "Frontend will be available at http://localhost:3000"
    npx vite --host 0.0.0.0 > "$PROJECT_DIR/frontend.log" 2>&1 &

    FRONTEND_PID=$!
    echo $FRONTEND_PID > "$PROJECT_DIR/.frontend.pid"
    ok "Frontend service started (PID: $FRONTEND_PID)"
    info "Log file: $PROJECT_DIR/frontend.log"
}

# =============================================================
# Stop All Services
# =============================================================
stop_all() {
    info "Stopping all services..."

    if [ -f "$PROJECT_DIR/.backend.pid" ]; then
        PID=$(cat "$PROJECT_DIR/.backend.pid")
        kill "$PID" 2>/dev/null && ok "Backend service stopped (PID: $PID)" || warn "Backend process $PID does not exist"
        rm -f "$PROJECT_DIR/.backend.pid"
    fi

    if [ -f "$PROJECT_DIR/.frontend.pid" ]; then
        PID=$(cat "$PROJECT_DIR/.frontend.pid")
        kill "$PID" 2>/dev/null && ok "Frontend service stopped (PID: $PID)" || warn "Frontend process $PID does not exist"
        rm -f "$PROJECT_DIR/.frontend.pid"
    fi

    # Find and kill any lingering Vite / Spring Boot processes
    lsof -ti:3000 2>/dev/null | xargs kill 2>/dev/null || true
    lsof -ti:8080 2>/dev/null | xargs kill 2>/dev/null || true

    ok "All services stopped"
}

# =============================================================
# Show Service Status
# =============================================================
show_status() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Cinema Booking System - Service Status${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "  Frontend: $(lsof -ti:3000 2>/dev/null && echo -e "${GREEN}Running${NC}" || echo -e "${RED}Stopped${NC}")  http://localhost:3000"
    echo -e "  Backend:  $(lsof -ti:8080 2>/dev/null && echo -e "${GREEN}Running${NC}" || echo -e "${RED}Stopped${NC}")  http://localhost:8080"
    echo -e "  API:      http://localhost:8080/swagger-ui.html"
    echo ""
    echo -e "  Default admin account: admin / 123456"
    echo -e "  Default user accounts: alice / 123456"
    echo -e "                         bob  / 123456"
    echo ""
    echo -e "  Stop services: ${CYAN}./start.sh --stop${NC}"
    echo ""
}

# =============================================================
# Main Function
# =============================================================
main() {
    # If no arguments, start everything
    if [ $# -eq 0 ]; then
        check_prerequisites
        init_database
        start_backend
        start_frontend
        # Wait for services to be ready
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
            echo "Usage: ./start.sh [options]"
            echo ""
            echo "Options:"
            echo "  -b, --backend-only     Start backend only"
            echo "  -f, --frontend-only    Start frontend only"
            echo "  -d, --db-only          Initialise database only"
            echo "  --stop                 Stop all services"
            echo "  -h, --help             Show this help message"
            echo ""
            echo "Default (no args) starts all services (database + backend + frontend)"
            ;;
        *)
            err "Unknown option: $1"
            echo "Usage: ./start.sh [-b|--backend-only] [-f|--frontend-only] [-d|--db-only] [--stop] [-h|--help]"
            exit 1
            ;;
    esac
}

main "$@"
