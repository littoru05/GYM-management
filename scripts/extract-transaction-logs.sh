#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="gym-management-backend"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPORTS_DIR="${REPO_ROOT}/docs/testing/reports/logs"
LOCAL_LOG_PATH="${REPO_ROOT}/backend/logs/gym-backend.log"
LINES=1000
ALL_MODE=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --all)
            ALL_MODE=1
            shift
            ;;
        --lines)
            LINES="$2"
            shift 2
            ;;
        --container)
            CONTAINER_NAME="$2"
            shift 2
            ;;
        --local-log)
            LOCAL_LOG_PATH="$2"
            shift 2
            ;;
        *)
            shift
            ;;
    esac
done

mkdir -p "${REPORTS_DIR}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
SUFFIX="summary"
if [ "${ALL_MODE}" -eq 1 ]; then
    SUFFIX="full"
fi
OUTPUT_FILE="${REPORTS_DIR}/txn-rollback-${TIMESTAMP}-${SUFFIX}.log"

echo "================================================================================"
echo " [LOG AUDIT] Extracting transaction logs for audit..."
echo "================================================================================"

RAW_LOG=$(mktemp)
SOURCE_DESC="NONE"

if command -v docker >/dev/null 2>&1 && docker inspect --format '{{.State.Status}}' "${CONTAINER_NAME}" 2>/dev/null | grep -q "running"; then
    SOURCE_DESC="Docker (${CONTAINER_NAME})"
    echo "Fetching logs from Docker container '${CONTAINER_NAME}' (tail ${LINES})..."
    docker logs --tail "${LINES}" "${CONTAINER_NAME}" > "${RAW_LOG}" 2>&1 || true
elif [ -f "${LOCAL_LOG_PATH}" ]; then
    SOURCE_DESC="Local File (${LOCAL_LOG_PATH})"
    echo "Reading logs from local file '${LOCAL_LOG_PATH}' (tail ${LINES})..."
    tail -n "${LINES}" "${LOCAL_LOG_PATH}" > "${RAW_LOG}"
else
    echo "[WARNING] Neither running Docker container '${CONTAINER_NAME}' nor local log file '${LOCAL_LOG_PATH}' was found." >&2
    rm -f "${RAW_LOG}"
    exit 1
fi

TOTAL_LINES=$(wc -l < "${RAW_LOG}" | tr -d ' ')
FILTERED_LOG=$(mktemp)

if [ "${ALL_MODE}" -eq 1 ]; then
    cp "${RAW_LOG}" "${FILTERED_LOG}"
else
    grep -Ei "JpaTransactionManager|Initiating transaction rollback|Rolling back JPA transaction|Creating new transaction|Completed transaction|POS Checkout|InsufficientStockException|PosOrderServiceImpl|MemberSubscriptionServiceImpl|VoucherServiceImpl|voucher_usages|voucher|ERROR|Exception|rollback" "${RAW_LOG}" > "${FILTERED_LOG}" || true
fi

MATCHED_LINES=$(wc -l < "${FILTERED_LOG}" | tr -d ' ')

ROLLBACK_COUNT=$(grep -Eic "rollback|Rolling back JPA transaction" "${RAW_LOG}" || true)
STOCK_ERR_COUNT=$(grep -Eic "InsufficientStockException|không đủ tồn kho" "${RAW_LOG}" || true)
POS_COUNT=$(grep -Eic "Bắt đầu xử lý POS Checkout|Tạo đơn hàng POS thành công" "${RAW_LOG}" || true)
VOUCHER_COUNT=$(grep -Eic "voucher|Voucher|voucher_usages" "${RAW_LOG}" || true)
ERR_COUNT=$(grep -Eic "ERROR|Exception" "${RAW_LOG}" || true)

{
    echo "================================================================================"
    echo " GYM MANAGEMENT - TRANSACTION & ROLLBACK AUDIT LOG EXTRACT"
    echo "================================================================================"
    echo " Extracted At   : $(date +"%Y-%m-%d %H:%M:%S")"
    echo " Log Source     : ${SOURCE_DESC}"
    echo " Mode           : $( [ "${ALL_MODE}" -eq 1 ] && echo "FULL RAW DUMP" || echo "FILTERED TRANSACTION AUDIT" )"
    echo " Lines Scanned  : ${TOTAL_LINES}"
    echo " Lines Matched  : ${MATCHED_LINES}"
    echo "--------------------------------------------------------------------------------"
    echo " SUMMARY METRICS FOR TESTER AUDIT:"
    echo "  - Transaction Rollbacks Detected : ${ROLLBACK_COUNT}"
    echo "  - Out-of-Stock (Stock Rollbacks) : ${STOCK_ERR_COUNT}"
    echo "  - POS Checkout Events            : ${POS_COUNT}"
    echo "  - Voucher Related Events         : ${VOUCHER_COUNT}"
    echo "  - Exceptions / Errors Recorded   : ${ERR_COUNT}"
    echo "================================================================================"
    echo ""
    cat "${FILTERED_LOG}"
} > "${OUTPUT_FILE}"

rm -f "${RAW_LOG}" "${FILTERED_LOG}"

echo "================================================================================"
echo " [SUCCESS] Transaction logs extracted successfully!"
echo " Source         : ${SOURCE_DESC}"
echo " Lines Scanned  : ${TOTAL_LINES}"
echo " Lines Extracted: ${MATCHED_LINES}"
echo "--------------------------------------------------------------------------------"
echo " AUDIT SUMMARY METRICS:"
echo "  * Transaction Rollbacks : ${ROLLBACK_COUNT}"
echo "  * Stock Exceptions      : ${STOCK_ERR_COUNT}"
echo "  * POS Checkout Events   : ${POS_COUNT}"
echo "  * Voucher Events        : ${VOUCHER_COUNT}"
echo "  * Errors / Exceptions   : ${ERR_COUNT}"
echo "--------------------------------------------------------------------------------"
echo " [SAVED] Log file evidence: ${OUTPUT_FILE}"
echo "================================================================================"
