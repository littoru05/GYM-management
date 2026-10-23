#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="${1:-gym-management-mysql}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${REPO_ROOT}/backend"

echo "================================================================================"
echo " [RESET DB] Recreating test database on container '${CONTAINER_NAME}'..."
echo "================================================================================"

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERROR] Docker CLI is not available. Please start Docker and retry." >&2
    exit 1
fi

if ! docker inspect "${CONTAINER_NAME}" >/dev/null 2>&1; then
    echo "[ERROR] Container '${CONTAINER_NAME}' not found. Start it with: docker compose up -d mysql" >&2
    exit 1
fi

CONTAINER_STATUS="$(docker inspect --format '{{.State.Status}}' "${CONTAINER_NAME}")"
if [ "${CONTAINER_STATUS}" != "running" ]; then
    echo "Starting container '${CONTAINER_NAME}'..."
    docker start "${CONTAINER_NAME}" >/dev/null
fi

echo "Waiting for MySQL in '${CONTAINER_NAME}' to be ready..."
READY=0
for _ in {1..30}; do
    if docker exec "${CONTAINER_NAME}" sh -ec 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysqladmin --protocol=socket --user=root ping --silent' >/dev/null 2>&1; then
        READY=1
        break
    fi
    sleep 0.5
done

if [ "${READY}" -ne 1 ]; then
    echo "[ERROR] MySQL in '${CONTAINER_NAME}' did not become ready in time." >&2
    exit 1
fi

START_TIME=$(date +%s)

echo "Recreating database gym_management..."
docker exec -i "${CONTAINER_NAME}" sh -ec 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysql --protocol=socket --user=root -e "DROP DATABASE IF EXISTS gym_management; CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"'

echo "Applying Stage 4 migrations and dev seeds directly via socket..."
(
    echo "SET NAMES 'utf8mb4'; SET FOREIGN_KEY_CHECKS = 0;"
    cat "${BACKEND_DIR}/src/main/resources/db/migration/V1__init.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration/V2__add_staff_to_check_ins.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration/V3__add_min_order_amount_to_promotions.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration/V4__add_voucher_features.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration-dev/V900__insert_demo_data.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration-dev/V901__hash_demo_passwords.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration-dev/V903__seed_inventory_and_pos_demo_data.sql"
    echo ""
    cat "${BACKEND_DIR}/src/main/resources/db/migration-dev/V904__create_voucher_and_staff_status.sql"
    echo ""
    echo "SET FOREIGN_KEY_CHECKS = 1;"
    cat << 'EOF'
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO flyway_schema_history 
(installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
(1, '1', 'init', 'SQL', 'V1__init.sql', NULL, 'root', 5, 1),
(2, '2', 'add staff to check ins', 'SQL', 'V2__add_staff_to_check_ins.sql', NULL, 'root', 5, 1),
(3, '3', 'add min order amount to promotions', 'SQL', 'V3__add_min_order_amount_to_promotions.sql', NULL, 'root', 5, 1),
(4, '4', 'add voucher features', 'SQL', 'V4__add_voucher_features.sql', NULL, 'root', 5, 1),
(5, '900', 'insert demo data', 'SQL', 'V900__insert_demo_data.sql', NULL, 'root', 5, 1),
(6, '901', 'hash demo passwords', 'SQL', 'V901__hash_demo_passwords.sql', NULL, 'root', 5, 1),
(7, '903', 'seed inventory and pos demo data', 'SQL', 'V903__seed_inventory_and_pos_demo_data.sql', NULL, 'root', 5, 1),
(8, '904', 'create voucher and staff status', 'SQL', 'V904__create_voucher_and_staff_status.sql', NULL, 'root', 5, 1);
EOF
) | docker exec -i "${CONTAINER_NAME}" sh -ec 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysql --protocol=socket --user=root --batch --raw --database=gym_management'

# Assertions
run_sql() {
    docker exec -i "${CONTAINER_NAME}" sh -ec "export MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\"; mysql --protocol=socket --user=root --batch --raw --skip-column-names --database=gym_management -e \"$1\""
}

PROD_VAL="$(run_sql "SELECT CONCAT(COUNT(*), '|', SUM(stock BETWEEN 50 AND 100), '|', SUM(stock <= min_stock AND status = 'ACTIVE'), '|', SUM(stock IN (1, 2)), '|', SUM(stock = 0 AND status = 'ACTIVE'), '|', SUM(status = 'INACTIVE'), '|', (SELECT COUNT(*) FROM categories WHERE code IN ('CAT-SUPPLEMENT', 'CAT-BEVERAGE', 'CAT-ACCESSORY')), '|', (SELECT COUNT(*) FROM products p LEFT JOIN categories c ON c.id = p.category_id WHERE c.id IS NULL)) FROM products WHERE sku IN ('SUP-WHEY-VAN-001', 'SUP-CREATINE-300-001', 'DRK-ELECTRO-ORANGE-001', 'ACC-GLOVE-GYM-001', 'ACC-SHAKER-700-001', 'SUP-WHEY-CHOCO-001', 'SUP-CREATINE-100-001', 'DRK-ELECTRO-LEMON-001', 'ACC-RESISTANCE-BAND-001', 'ACC-SPEED-ROPE-001');")"
[ "${PROD_VAL}" = "10|4|5|2|1|1|3|0" ] || { echo "[ERROR] Product assertion failed: ${PROD_VAL}" >&2; exit 1; }

STAFF_VAL="$(run_sql "SELECT CONCAT(COUNT(*), '|', SUM(status = 'ACTIVE'), '|', SUM(status = 'LOCKED'), '|', SUM(status = 'INACTIVE')) FROM users WHERE code IN ('STF-002', 'STF-003', 'STF-004');")"
[ "${STAFF_VAL}" = "3|1|1|1" ] || { echo "[ERROR] Staff assertion failed: ${STAFF_VAL}" >&2; exit 1; }

VOUCHER_VAL="$(run_sql "SELECT CONCAT(COUNT(*), '|', SUM(discount_type = 'PERCENTAGE'), '|', SUM(discount_type = 'FIXED'), '|', SUM(status = 'ACTIVE'), '|', SUM(status = 'INACTIVE')) FROM promotions WHERE code IN ('DISCOUNT20_CAP50K', 'FIXED100K_MIN500K', 'EXHAUSTED_VOUCHER', 'EXPIRED_VOUCHER', 'FUTURE_VOUCHER', 'INACTIVE_VOUCHER');")"
[ "${VOUCHER_VAL}" = "6|4|2|5|1" ] || { echo "[ERROR] Voucher assertion failed: ${VOUCHER_VAL}" >&2; exit 1; }

STAGE4_VAL="$(run_sql "SELECT CONCAT((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'gym_management' AND table_name = 'voucher_usages'), '|', (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'gym_management' AND table_name = 'promotions' AND column_name IN ('applicable_to_all', 'applicable_membership_ids')));")"
[ "${STAGE4_VAL}" = "1|2" ] || { echo "[ERROR] Stage 4 schema assertion failed: ${STAGE4_VAL}" >&2; exit 1; }

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo "================================================================================"
echo " [SUCCESS] Database 'gym_management' restored successfully in ~${DURATION}s!"
echo " * Verified: 10 products, 3 staff accounts, 6 test vouchers"
echo " * Stage 4 : voucher_usages table and promotion columns verified"
echo "================================================================================"
