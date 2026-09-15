#!/bin/bash
# Nadi - Production Deployment Script
# Usage: ./deploy.sh [up|down|restart|logs|status]

set -euo pipefail

COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env.prod"

usage() {
    echo "Usage: $0 [up|down|restart|logs|status|backup|restore <file>]"
    exit 1
}

check_prerequisites() {
    if ! command -v docker &> /dev/null; then
        echo "Error: Docker not installed"
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        echo "Error: Docker Compose not installed"
        exit 1
    fi
    if [ ! -f "${ENV_FILE}" ]; then
        echo "Warning: ${ENV_FILE} not found, using defaults"
        ENV_FILE=""
    fi
}

deploy_up() {
    echo "Starting Nadi in production mode..."
    if [ -n "${ENV_FILE}" ]; then
        docker-compose --env-file "${ENV_FILE}" up -d --build
    else
        docker-compose up -d --build
    fi
    echo "Nadi is running!"
    echo "  Frontend: http://localhost"
    echo "  Backend:  http://localhost:8080/api"
    echo "  Health:   http://localhost:8080/api/actuator/health"
}

deploy_down() {
    echo "Stopping Nadi..."
    docker-compose down
    echo "Nadi stopped."
}

deploy_restart() {
    deploy_down
    deploy_up
}

deploy_logs() {
    docker-compose logs -f backend
}

deploy_status() {
    docker-compose ps
}

deploy_backup() {
    echo "Starting backup..."
    docker-compose exec backup /scripts/backup.sh manual
    echo "Backup complete."
}

deploy_restore() {
    if [ $# -lt 2 ]; then
        echo "Usage: $0 restore <backup_file>"
        exit 1
    fi
    echo "Restoring from $2..."
    docker-compose exec backup /scripts/restore.sh "/backups/$2"
    echo "Restore complete."
}

check_prerequisites

case "${1:-}" in
    up) deploy_up ;;
    down) deploy_down ;;
    restart) deploy_restart ;;
    logs) deploy_logs ;;
    status) deploy_status ;;
    backup) deploy_backup ;;
    restore) deploy_restore "$@" ;;
    *) usage ;;
esac
