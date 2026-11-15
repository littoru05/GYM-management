#!/usr/bin/env bash
# ==============================================================================
# Script: check-ha-health.sh
# Multi-Tier High Availability Health Check for GYM Management System
# Tiers:
#   Tier 1: AWS Infrastructure (ALB Target Group Health & ECS Services)
#   Tier 2: Deep Backend Health (/actuator/health with MySQL & diskSpace)
#   Tier 3: Frontend Web SPA (Root HTTP 200 & /version.json Artifact Version)
# ==============================================================================

set -eo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="${ENVIRONMENT:-prod}"
AWS_REGION="${AWS_REGION:-ap-southeast-1}"
DEFAULT_URL="${APP_URL:-http://gym-management.duckdns.org}"
BASE_URL="${DEFAULT_URL}"
TIMEOUT_SEC=5
HTTP_ONLY=false
VERBOSE=false

usage() {
    cat <<EOF
${BOLD}Usage:${NC} $0 [options]

${BOLD}Options:${NC}
  --url, -u <url>         Base URL to test (default: ${DEFAULT_URL})
  --env, -e <env>         Target environment: prod | staging (default: prod)
  --region, -r <region>   AWS region (default: ap-southeast-1)
  --timeout, -t <sec>     HTTP timeout in seconds (default: 5)
  --http-only             Skip Tier 1 AWS CLI checks and only test Tier 2 & Tier 3 via HTTP
  --verbose, -v           Enable verbose diagnostics output
  --help, -h              Show this help message and exit

${BOLD}Examples:${NC}
  $0 --http-only
  $0 --url http://gym-management.duckdns.org
  $0 --env prod --region ap-southeast-1
EOF
    exit 0
}

# Parse CLI arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --help|-h)
            usage
            ;;
        --http-only)
            HTTP_ONLY=true
            shift
            ;;
        --url|-u)
            BASE_URL="$2"
            shift 2
            ;;
        --env|-e)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --region|-r)
            AWS_REGION="$2"
            shift 2
            ;;
        --timeout|-t)
            TIMEOUT_SEC="$2"
            shift 2
            ;;
        --verbose|-v)
            VERBOSE=true
            shift
            ;;
        *)
            echo -e "${RED}[ERROR] Unknown option: $1${NC}" >&2
            usage
            ;;
    esac
done

# Strip trailing slash from BASE_URL
BASE_URL="${BASE_URL%/}"

echo -e "${CYAN}======================================================================${NC}"
echo -e "${CYAN}${BOLD}   GYM MANAGEMENT - MULTI-TIER HIGH AVAILABILITY HEALTH MONITOR   ${NC}"
echo -e "${CYAN}======================================================================${NC}"
echo -e "Environment : ${BOLD}${ENVIRONMENT}${NC}"
echo -e "AWS Region  : ${BOLD}${AWS_REGION}${NC}"
echo -e "Target URL  : ${BOLD}${BASE_URL}${NC}"
echo -e "Mode        : ${BOLD}$([ "$HTTP_ONLY" = true ] && echo "HTTP-Only (Tier 2 & 3)" || echo "Full Multi-Tier (Tier 1, 2, 3)")${NC}"
echo -e "Timeout     : ${BOLD}${TIMEOUT_SEC}s${NC}"
echo -e "${CYAN}----------------------------------------------------------------------${NC}\n"

OVERALL_PASS=true

# ==============================================================================
# TIER 1: AWS INFRASTRUCTURE (ALB Target Groups & ECS Services)
# ==============================================================================
if [ "$HTTP_ONLY" = true ]; then
    echo -e "${YELLOW}[TIER 1 SKIPPED] AWS Infrastructure check bypassed via --http-only flag.${NC}\n"
else
    echo -e "${BOLD}[TIER 1] AWS Infrastructure Health (ALB Target Group & ECS Services)...${NC}"
    
    if ! command -v aws &> /dev/null; then
        echo -e "${RED}  ✗ AWS CLI not found in PATH. Install AWS CLI or run with --http-only.${NC}"
        OVERALL_PASS=false
    else
        CLUSTER_NAME="gym-${ENVIRONMENT}-cluster"
        BACKEND_TG_NAME="gym-${ENVIRONMENT}-backend-tg"
        FRONTEND_TG_NAME="gym-${ENVIRONMENT}-frontend-tg"
        BACKEND_SERVICE="gym-${ENVIRONMENT}-backend"
        FRONTEND_SERVICE="gym-${ENVIRONMENT}-frontend"

        # Check ALB Target Groups
        echo -e "  Checking ALB Target Groups..."
        for TG_NAME in "$BACKEND_TG_NAME" "$FRONTEND_TG_NAME"; do
            TG_ARN=$(aws elbv2 describe-target-groups --names "$TG_NAME" --region "$AWS_REGION" --query "TargetGroups[0].TargetGroupArn" --output text 2>/dev/null || true)
            if [ -n "$TG_ARN" ] && [ "$TG_ARN" != "None" ]; then
                HEALTH_JSON=$(aws elbv2 describe-target-health --target-group-arn "$TG_ARN" --region "$AWS_REGION" --output json 2>/dev/null || true)
                HEALTHY_COUNT=$(echo "$HEALTH_JSON" | grep -c '"State": "healthy"' || true)
                UNHEALTHY_COUNT=$(echo "$HEALTH_JSON" | grep -c '"State": "unhealthy"' || true)
                TOTAL_TARGETS=$(echo "$HEALTH_JSON" | grep -c '"State":' || true)

                if [ "$HEALTHY_COUNT" -gt 0 ] && [ "$UNHEALTHY_COUNT" -eq 0 ]; then
                    echo -e "    ${GREEN}✓ Target Group [${TG_NAME}]: Healthy (${HEALTHY_COUNT}/${TOTAL_TARGETS} targets healthy)${NC}"
                else
                    echo -e "    ${RED}✗ Target Group [${TG_NAME}]: Warning/Unhealthy (Healthy: ${HEALTHY_COUNT}, Unhealthy: ${UNHEALTHY_COUNT})${NC}"
                    OVERALL_PASS=false
                fi
            else
                echo -e "    ${YELLOW}⚠ Target Group [${TG_NAME}]: Not found or access denied in region ${AWS_REGION}${NC}"
                OVERALL_PASS=false
            fi
        done

        # Check ECS Services and Tasks
        echo -e "  Checking ECS Services in cluster [${CLUSTER_NAME}]..."
        SERVICES_JSON=$(aws ecs describe-services --cluster "$CLUSTER_NAME" --services "$BACKEND_SERVICE" "$FRONTEND_SERVICE" --region "$AWS_REGION" --output json 2>/dev/null || true)
        
        if [ -n "$SERVICES_JSON" ]; then
            for SVC in "$BACKEND_SERVICE" "$FRONTEND_SERVICE"; do
                SVC_DATA=$(echo "$SERVICES_JSON" | grep -A 10 "\"serviceName\": \"${SVC}\"" || true)
                DESIRED=$(echo "$SVC_DATA" | grep '"desiredCount":' | head -1 | awk -F': ' '{print $2}' | tr -d ', ' || true)
                RUNNING=$(echo "$SVC_DATA" | grep '"runningCount":' | head -1 | awk -F': ' '{print $2}' | tr -d ', ' || true)

                if [ -n "$DESIRED" ] && [ -n "$RUNNING" ] && [ "$RUNNING" -ge "$DESIRED" ] && [ "$DESIRED" -gt 0 ]; then
                    echo -e "    ${GREEN}✓ ECS Service [${SVC}]: Running (${RUNNING}/${DESIRED} tasks running)${NC}"
                else
                    echo -e "    ${RED}✗ ECS Service [${SVC}]: Degraded (${RUNNING:-0}/${DESIRED:-0} tasks running)${NC}"
                    OVERALL_PASS=false
                fi
            done

            # Query individual active ECS tasks state
            TASK_ARNS=$(aws ecs list-tasks --cluster "$CLUSTER_NAME" --region "$AWS_REGION" --query "taskArns[]" --output text 2>/dev/null || true)
            if [ -n "$TASK_ARNS" ] && [ "$TASK_ARNS" != "None" ]; then
                echo -e "    ${GREEN}✓ Active ECS Tasks detected in cluster: $(echo "$TASK_ARNS" | wc -w) task(s)${NC}"
            fi
        else
            echo -e "    ${YELLOW}⚠ ECS Cluster or services could not be described.${NC}"
            OVERALL_PASS=false
        fi
    fi
    echo ""
fi

# ==============================================================================
# TIER 2: DEEP BACKEND HEALTH (/actuator/health)
# ==============================================================================
echo -e "${BOLD}[TIER 2] Deep Backend Actuator Health Probe...${NC}"
ACTUATOR_URL="${BASE_URL}/actuator/health"
echo -e "  Probing: ${ACTUATOR_URL}"

HTTP_CODE=$(curl -s -m "$TIMEOUT_SEC" -o /tmp/actuator_health.json -w "%{http_code}" "$ACTUATOR_URL" 2>/dev/null || echo "000")
BODY_CONTENT=$(cat /tmp/actuator_health.json 2>/dev/null || echo "")

if [ "$HTTP_CODE" = "200" ]; then
    STATUS_VAL=$(echo "$BODY_CONTENT" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4 || true)
    if [ -z "$STATUS_VAL" ]; then
        STATUS_VAL=$(echo "$BODY_CONTENT" | grep -o '"status": "[^"]*"' | head -1 | cut -d'"' -f4 || true)
    fi

    # Check deep component health (db and diskSpace)
    HAS_DB_UP=false
    HAS_DISK_UP=false

    if echo "$BODY_CONTENT" | grep -qi '"db":.*"status":\s*"UP"'; then
        HAS_DB_UP=true
    fi
    if echo "$BODY_CONTENT" | grep -qi '"diskSpace":.*"status":\s*"UP"'; then
        HAS_DISK_UP=true
    fi

    if [ "$STATUS_VAL" = "UP" ]; then
        echo -e "  ${GREEN}✓ Backend Actuator Status : UP (HTTP 200)${NC}"
        if [ "$HAS_DB_UP" = true ]; then
            echo -e "  ${GREEN}✓ MySQL Database Component : UP (HikariCP pool active)${NC}"
        else
            echo -e "  ${RED}✗ MySQL Database Component : Component status not UP${NC}"
            OVERALL_PASS=false
        fi

        if [ "$HAS_DISK_UP" = true ]; then
            echo -e "  ${GREEN}✓ Disk Space Component     : UP (Free space threshold ok)${NC}"
        else
            echo -e "  ${RED}✗ Disk Space Component     : Component status not UP${NC}"
            OVERALL_PASS=false
        fi
    else
        echo -e "  ${RED}✗ Backend Actuator Status : ${STATUS_VAL:-UNKNOWN} (Expected: UP)${NC}"
        OVERALL_PASS=false
    fi
else
    echo -e "  ${RED}✗ Backend Actuator Probe Failed : HTTP Code ${HTTP_CODE}${NC}"
    if [ "$VERBOSE" = true ] && [ -n "$BODY_CONTENT" ]; then
        echo -e "  Response payload: ${BODY_CONTENT}"
    fi
    OVERALL_PASS=false
fi
echo ""

# ==============================================================================
# TIER 3: FRONTEND WEB (Root / and /version.json)
# ==============================================================================
echo -e "${BOLD}[TIER 3] Frontend Web Readiness & Version Probe...${NC}"
ROOT_URL="${BASE_URL}/"
VERSION_URL="${BASE_URL}/version.json"

echo -e "  Probing Root SPA: ${ROOT_URL}"
FE_HTTP_CODE=$(curl -s -m "$TIMEOUT_SEC" -o /dev/null -w "%{http_code}" "$ROOT_URL" 2>/dev/null || echo "000")

if [ "$FE_HTTP_CODE" = "200" ]; then
    echo -e "  ${GREEN}✓ Frontend Root SPA       : HTTP 200 OK${NC}"
else
    echo -e "  ${RED}✗ Frontend Root SPA Failed: HTTP Code ${FE_HTTP_CODE}${NC}"
    OVERALL_PASS=false
fi

echo -e "  Probing Manifest: ${VERSION_URL}"
VER_HTTP_CODE=$(curl -s -m "$TIMEOUT_SEC" -o /tmp/version_health.json -w "%{http_code}" "$VERSION_URL" 2>/dev/null || echo "000")
VER_CONTENT=$(cat /tmp/version_health.json 2>/dev/null || echo "")

if [ "$VER_HTTP_CODE" = "200" ]; then
    APP_VER=$(echo "$VER_CONTENT" | grep -o '"version":\s*"[^"]*"' | head -1 | cut -d'"' -f4 || true)
    COMMIT_SHA=$(echo "$VER_CONTENT" | grep -o '"commitSha":\s*"[^"]*"' | head -1 | cut -d'"' -f4 || true)
    echo -e "  ${GREEN}✓ Artifact Version        : ${APP_VER:-N/A}${NC}"
    if [ -n "$COMMIT_SHA" ]; then
        echo -e "  ${GREEN}✓ Build Commit SHA        : ${COMMIT_SHA}${NC}"
    fi
else
    echo -e "  ${RED}✗ Version Manifest Failed : HTTP Code ${VER_HTTP_CODE} (/version.json)${NC}"
    OVERALL_PASS=false
fi
echo ""

# ==============================================================================
# FINAL HEALTH SUMMARY
# ==============================================================================
echo -e "${CYAN}======================================================================${NC}"
if [ "$OVERALL_PASS" = true ]; then
    echo -e "${GREEN}${BOLD}   [PASS] ALL CHECKED HA HEALTH TIERS ARE HEALTHY & OPERATIONAL   ${NC}"
    echo -e "${CYAN}======================================================================${NC}"
    exit 0
else
    echo -e "${RED}${BOLD}   [FAIL] ONE OR MORE HA HEALTH TIERS REPORTED DEGRADED STATE    ${NC}"
    echo -e "${CYAN}======================================================================${NC}"
    exit 1
fi
