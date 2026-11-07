#!/usr/bin/env bash
# ==============================================================================
# deploy-prod.sh - Production Release & Self-Healing Auto-Rollback Orchestrator
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

# Configuration defaults
COMPOSE_FILE="docker-compose.prod.yml"
STATE_FILE=".deploy-state"
HISTORY_LOG=".deploy-history.log"
MYSQL_CONTAINER="gym-prod-mysql"
BACKEND_CONTAINER="gym-prod-backend"
FRONTEND_CONTAINER="gym-prod-frontend"
DB_NAME="${DB_NAME:-gym_management}"
DB_PASSWORD="${DB_PASSWORD:-123456}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
BACKUP_RETENTION_COUNT="${BACKUP_RETENTION_COUNT:-10}"
HEALTHCHECK_TIMEOUT_SECONDS="${HEALTHCHECK_TIMEOUT_SECONDS:-90}"
HEALTHCHECK_INTERVAL=5

RESTORE_BACKUP_FILE=""
TARGET_APP_VERSION=""

# ------------------------------------------------------------------------------
# Helper functions
# ------------------------------------------------------------------------------
wait_for_mysql() {
    local max_wait=30
    local ready=0
    for _ in $(seq 1 "${max_wait}"); do
        if docker exec "${MYSQL_CONTAINER}" sh -ec "export MYSQL_PWD='${DB_PASSWORD}'; mysqladmin --protocol=socket --user=root ping --silent" >/dev/null 2>&1; then
            ready=1
            break
        fi
        sleep 1
    done
    if [ "${ready}" -ne 1 ]; then
        echo "[ERROR] MySQL container '${MYSQL_CONTAINER}' is not responsive." >&2
        return 1
    fi
    return 0
}

check_actuator_health() {
    local container="$1"
    # Verify HTTP 200 and top-level "status":"UP" (prevent false positives on nested component states)
    local output
    output="$(docker exec "${container}" wget --server-response -qO- http://127.0.0.1:8080/actuator/health 2>&1 || true)"
    if echo "${output}" | grep -q "HTTP/[0-9.]* 200" && echo "${output}" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"'; then
        return 0
    fi
    return 1
}

check_frontend_health() {
    local container="$1"
    if docker exec "${container}" wget -q --spider http://127.0.0.1:80 2>/dev/null; then
        return 0
    fi
    return 1
}

validate_sql_dump_archive() {
    local archive_path="$1"
    if [ ! -f "${archive_path}" ]; then
        echo "[ERROR] Backup file does not exist: ${archive_path}" >&2
        return 1
    fi
    if [ ! -s "${archive_path}" ]; then
        echo "[ERROR] Backup archive is empty (0 bytes): ${archive_path}" >&2
        return 1
    fi
    if ! gzip -t "${archive_path}" 2>/dev/null; then
        echo "[ERROR] Backup archive failed gzip integrity test: ${archive_path}" >&2
        return 1
    fi
    # Verify presence of valid SQL dump header in decompressed stream
    if ! gzip -dc "${archive_path}" | head -n 30 | grep -Eq "(MySQL dump|Server version|CREATE TABLE|SET @)"; then
        echo "[ERROR] Backup archive does not contain a valid SQL dump header: ${archive_path}" >&2
        return 1
    fi
    return 0
}

# ------------------------------------------------------------------------------
# Argument parsing
# ------------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
    case "$1" in
        --restore-backup)
            RESTORE_BACKUP_FILE="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [APP_VERSION] [--restore-backup <file.sql.gz>]"
            exit 0
            ;;
        *)
            if [ -z "${TARGET_APP_VERSION}" ]; then
                TARGET_APP_VERSION="$1"
                shift
            else
                echo "[ERROR] Unknown argument: $1" >&2
                exit 1
            fi
            ;;
    esac
done

# ------------------------------------------------------------------------------
# 1. One-command Disaster Recovery: --restore-backup
# ------------------------------------------------------------------------------
if [ -n "${RESTORE_BACKUP_FILE}" ]; then
    echo "================================================================================"
    echo " [EMERGENCY RESTORE] Initiating database restore from: ${RESTORE_BACKUP_FILE}"
    echo "================================================================================"

    if ! validate_sql_dump_archive "${RESTORE_BACKUP_FILE}"; then
        exit 1
    fi

    echo "Verifying MySQL container readiness..."
    if ! wait_for_mysql; then
        exit 1
    fi

    echo "Decompressing and restoring SQL dump into MySQL database '${DB_NAME}'..."
    if gzip -dc "${RESTORE_BACKUP_FILE}" | docker exec -i "${MYSQL_CONTAINER}" sh -ec "export MYSQL_PWD='${DB_PASSWORD}'; mysql --protocol=socket --user=root '${DB_NAME}'"; then
        echo "[SUCCESS] Database restored successfully from ${RESTORE_BACKUP_FILE}."
        exit 0
    else
        echo "[ERROR] Database restoration failed!" >&2
        exit 1
    fi
fi

# ------------------------------------------------------------------------------
# 2. Version & State Resolution
# ------------------------------------------------------------------------------
if [ -z "${TARGET_APP_VERSION}" ]; then
    if [ -n "${APP_VERSION:-}" ]; then
        TARGET_APP_VERSION="${APP_VERSION}"
    elif command -v git >/dev/null 2>&1 && git rev-parse --git-dir >/dev/null 2>&1; then
        GIT_SHA="$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")"
        TARGET_APP_VERSION="1.0.0-${GIT_SHA}"
    else
        TARGET_APP_VERSION="1.0.0-dev"
    fi
fi

export APP_VERSION="${TARGET_APP_VERSION}"

# Read LAST_STABLE_VERSION from .deploy-state
LAST_STABLE_VERSION=""
if [ -f "${STATE_FILE}" ]; then
    LAST_STABLE_VERSION="$(grep -E '^LAST_STABLE_VERSION=' "${STATE_FILE}" 2>/dev/null | cut -d '=' -f2- | tr -d '\r\n' || true)"
fi
if [ -z "${LAST_STABLE_VERSION}" ]; then
    LAST_STABLE_VERSION="${TARGET_APP_VERSION}"
fi

echo "================================================================================"
echo " [DEPLOY] Starting production deployment orchestrator"
echo " Target Artifact Version : ${APP_VERSION}"
echo " Last Stable Version     : ${LAST_STABLE_VERSION}"
echo " Compose Stack           : ${COMPOSE_FILE}"
echo " Backup Directory        : ${BACKUP_DIR}"
echo " Backup Retention Ring   : ${BACKUP_RETENTION_COUNT} snapshots"
echo " Health Timeout          : ${HEALTHCHECK_TIMEOUT_SECONDS}s (every ${HEALTHCHECK_INTERVAL}s)"
echo "================================================================================"

# ------------------------------------------------------------------------------
# 3. Ensure MySQL Readiness & Query Flyway Schema Version
# ------------------------------------------------------------------------------
echo "Verifying MySQL container readiness..."
if ! wait_for_mysql; then
    echo "[ERROR] Pre-deployment MySQL healthcheck failed. Aborting release immediately." >&2
    exit 1
fi

echo "Querying current schema version from flyway_schema_history..."
SCHEMA_QUERY="SELECT COALESCE(MAX(version), '0') FROM flyway_schema_history;"
SCHEMA_VER="$(docker exec "${MYSQL_CONTAINER}" sh -ec "export MYSQL_PWD='${DB_PASSWORD}'; mysql --protocol=socket --user=root --batch --skip-column-names -e \"${SCHEMA_QUERY}\" '${DB_NAME}'" 2>/dev/null || echo "0")"
SCHEMA_VER="$(echo "${SCHEMA_VER}" | tr -d '\r\n')"
if [ -z "${SCHEMA_VER}" ]; then
    SCHEMA_VER="0"
fi
echo "Current Flyway schema version: v${SCHEMA_VER}"

# ------------------------------------------------------------------------------
# 4. Pre-Migration Non-Locking Compressed Backup & Retention Pruning
# ------------------------------------------------------------------------------
mkdir -p "${BACKUP_DIR}"
TIMESTAMP="$(date +'%Y%m%d_%H%M%S')"
BACKUP_FILENAME="gym_db_v${SCHEMA_VER}_to_${APP_VERSION}_${TIMESTAMP}.sql.gz"
BACKUP_FILEPATH="${BACKUP_DIR}/${BACKUP_FILENAME}"

echo "Capturing non-locking pre-migration backup (${BACKUP_FILEPATH})..."
DUMP_CMD="export MYSQL_PWD='${DB_PASSWORD}'; mysqldump --protocol=socket --user=root --single-transaction --routines --triggers --events '${DB_NAME}'"

# Stream mysqldump (stdout only to avoid corrupting gzip with stderr warnings) through gzip -9
if ! docker exec "${MYSQL_CONTAINER}" sh -ec "${DUMP_CMD}" 2>/dev/null | gzip -9 > "${BACKUP_FILEPATH}"; then
    echo "[ERROR] Pre-migration mysqldump failed! Aborting release immediately." >&2
    rm -f "${BACKUP_FILEPATH}"
    exit 1
fi

# Verify backup size, gzip integrity, and SQL dump header
if ! validate_sql_dump_archive "${BACKUP_FILEPATH}"; then
    echo "[ERROR] Pre-migration backup validation failed! Aborting release immediately." >&2
    rm -f "${BACKUP_FILEPATH}"
    exit 1
fi

echo "[BACKUP VERIFIED] Snapshot created successfully: ${BACKUP_FILEPATH} ($(wc -c < "${BACKUP_FILEPATH}") bytes)"

# Enforce 10-snapshot retention ring (sorted by modification time, oldest first)
echo "Enforcing backup retention ring (max ${BACKUP_RETENTION_COUNT} snapshots)..."
while true; do
    COUNT="$(find "${BACKUP_DIR}" -maxdepth 1 -name "*.sql.gz" -type f | wc -l)"
    if [ "${COUNT}" -gt "${BACKUP_RETENTION_COUNT}" ]; then
        OLDEST="$(ls -1tr "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | head -n 1)"
        if [ -n "${OLDEST}" -a -f "${OLDEST}" ]; then
            echo "Pruning older backup: ${OLDEST}"
            rm -f "${OLDEST}"
        else
            break
        fi
    else
        break
    fi
done

# ------------------------------------------------------------------------------
# 5. Launch Target Release via Compose Stack
# ------------------------------------------------------------------------------
echo "Launching target Artifact Version ${APP_VERSION} via Compose..."
docker compose -f "${COMPOSE_FILE}" up -d

# ------------------------------------------------------------------------------
# 6. Deep Actuator Health & Frontend Readiness Polling Loop
# ------------------------------------------------------------------------------
echo "Polling health probes for up to ${HEALTHCHECK_TIMEOUT_SECONDS}s..."
MAX_ATTEMPTS=$(( HEALTHCHECK_TIMEOUT_SECONDS / HEALTHCHECK_INTERVAL ))
ATTEMPT=0
RELEASE_HEALTHY=0

while [ "${ATTEMPT}" -lt "${MAX_ATTEMPTS}" ]; do
    ATTEMPT=$(( ATTEMPT + 1 ))
    echo "Healthcheck probe attempt ${ATTEMPT}/${MAX_ATTEMPTS}..."

    # Check for immediate container crash or exit
    CONTAINER_STATE="$(docker inspect --format '{{.State.Status}}|{{.State.Restarting}}|{{.State.ExitCode}}' "${BACKEND_CONTAINER}" 2>/dev/null || echo "missing|false|1")"
    STATUS="$(echo "${CONTAINER_STATE}" | cut -d '|' -f1)"
    RESTARTING="$(echo "${CONTAINER_STATE}" | cut -d '|' -f2)"
    EXIT_CODE="$(echo "${CONTAINER_STATE}" | cut -d '|' -f3)"

    if [ "${STATUS}" = "exited" ] || [ "${STATUS}" = "dead" ] || [ "${RESTARTING}" = "true" ] || [ "${EXIT_CODE}" != "0" -a "${STATUS}" != "running" ]; then
        echo "[CRASH DETECTED] Backend container '${BACKEND_CONTAINER}' failed or exited unexpectedly (Status: ${STATUS}, ExitCode: ${EXIT_CODE})."
        break
    fi

    # Probe /actuator/health for HTTP 200 and "status":"UP"
    if check_actuator_health "${BACKEND_CONTAINER}" && check_frontend_health "${FRONTEND_CONTAINER}"; then
        RELEASE_HEALTHY=1
        echo "[HEALTHY] Both Backend /actuator/health and Frontend HTTP 200 verified healthy on attempt ${ATTEMPT}."
        break
    fi

    sleep "${HEALTHCHECK_INTERVAL}"
done

# ------------------------------------------------------------------------------
# 7. Release Evaluation: Success vs Auto-Rollback
# ------------------------------------------------------------------------------
LOG_TIMESTAMP="$(date +'%Y-%m-%d %H:%M:%S')"

if [ "${RELEASE_HEALTHY}" -eq 1 ]; then
    echo "================================================================================"
    echo " [RELEASE SUCCESS] Artifact Version ${APP_VERSION} is UP and healthy!"
    echo "================================================================================"

    # Persist LAST_STABLE_VERSION in .deploy-state
    echo "LAST_STABLE_VERSION=${APP_VERSION}" > "${STATE_FILE}"
    echo "Updated ${STATE_FILE} with LAST_STABLE_VERSION=${APP_VERSION}"

    # Record in .deploy-history.log
    echo "[${LOG_TIMESTAMP}] [SUCCESS] Version ${APP_VERSION} deployed successfully. Pre-migration backup: ${BACKUP_FILENAME}" >> "${HISTORY_LOG}"
    exit 0
else
    echo "================================================================================"
    echo " [RELEASE FAILED] Health verification timed out or container crashed!"
    echo " Printing last 50 lines of backend logs for diagnosis:"
    echo "================================================================================"
    docker logs --tail 50 "${BACKEND_CONTAINER}" 2>&1 || true

    echo "================================================================================"
    echo " [AUTO-ROLLBACK] Triggering automated rollback to LAST_STABLE_VERSION=${LAST_STABLE_VERSION}"
    echo "================================================================================"

    export APP_VERSION="${LAST_STABLE_VERSION}"
    docker compose -f "${COMPOSE_FILE}" up -d

    echo "Verifying rolled-back stack health (Backend and Frontend)..."
    ROLLBACK_HEALTHY=0
    for _ in $(seq 1 "${MAX_ATTEMPTS}"); do
        if check_actuator_health "${BACKEND_CONTAINER}" && check_frontend_health "${FRONTEND_CONTAINER}"; then
            ROLLBACK_HEALTHY=1
            break
        fi
        sleep "${HEALTHCHECK_INTERVAL}"
    done

    # Record rollback event in .deploy-history.log
    echo "[${LOG_TIMESTAMP}] [ROLLED_BACK] Version ${TARGET_APP_VERSION} failed health verification. Auto-rolled back to LAST_STABLE_VERSION=${LAST_STABLE_VERSION} (Rollback Healthy: ${ROLLBACK_HEALTHY})." >> "${HISTORY_LOG}"

    echo "[FAILURE] Release ${TARGET_APP_VERSION} failed. Rollback executed. Exit code 1." >&2
    exit 1
fi
