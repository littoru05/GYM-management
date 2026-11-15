#!/usr/bin/env bash
# ==============================================================================
# Script: quick-restart-prod.sh
# Emergency Quick Restart & Zero-Downtime Recovery Runbook Script
# Capabilities:
#   1. Automatic RDS MySQL status check and wake (start-db-instance & wait available)
#   2. Zero-downtime rolling task force-update on ECS Fargate (backend & frontend)
#   3. Automatic Dynamic DNS DuckDNS re-sync against active ALB IPv4 address if changed
# ==============================================================================

set -eo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

ENVIRONMENT="${ENVIRONMENT:-prod}"
AWS_REGION="${AWS_REGION:-ap-southeast-1}"
RDS_IDENTIFIER="gym-${ENVIRONMENT}-mysql"
ECS_CLUSTER="gym-${ENVIRONMENT}-cluster"
BACKEND_SERVICE="gym-${ENVIRONMENT}-backend"
FRONTEND_SERVICE="gym-${ENVIRONMENT}-frontend"
ALB_NAME="gym-${ENVIRONMENT}-alb"

DUCKDNS_DOMAIN="${DUCKDNS_PROD_DOMAIN:-${DUCKDNS_DOMAIN:-gym-management}}"
DUCKDNS_DOMAIN="${DUCKDNS_DOMAIN%.duckdns.org}"
DUCKDNS_TOKEN="${DUCKDNS_TOKEN:-}"

DRY_RUN=false
SKIP_RDS=false
SKIP_DNS=false

usage() {
    cat <<EOF
${BOLD}Usage:${NC} $0 [options]

${BOLD}Options:${NC}
  --env, -e <env>             Target environment: prod | staging (default: prod)
  --region, -r <region>       AWS region (default: ap-southeast-1)
  --dry-run                   Simulate actions without executing write commands
  --skip-rds                  Skip RDS check and wake step
  --skip-dns                  Skip DuckDNS Dynamic DNS synchronization
  --duckdns-domain <domain>   DuckDNS subdomain (default: gym-management or \$DUCKDNS_DOMAIN)
  --duckdns-token <token>     DuckDNS API token (default: \$DUCKDNS_TOKEN)
  --help, -h                  Show this help message and exit

${BOLD}Examples:${NC}
  $0 --dry-run
  $0 --env prod
  $0 --skip-rds
EOF
    exit 0
}

# Parse CLI arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --help|-h)
            usage
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --skip-rds)
            SKIP_RDS=true
            shift
            ;;
        --skip-dns)
            SKIP_DNS=true
            shift
            ;;
        --env|-e)
            ENVIRONMENT="$2"
            RDS_IDENTIFIER="gym-${ENVIRONMENT}-mysql"
            ECS_CLUSTER="gym-${ENVIRONMENT}-cluster"
            BACKEND_SERVICE="gym-${ENVIRONMENT}-backend"
            FRONTEND_SERVICE="gym-${ENVIRONMENT}-frontend"
            ALB_NAME="gym-${ENVIRONMENT}-alb"
            shift 2
            ;;
        --region|-r)
            AWS_REGION="$2"
            shift 2
            ;;
        --duckdns-domain)
            DUCKDNS_DOMAIN="${2%.duckdns.org}"
            shift 2
            ;;
        --duckdns-token)
            DUCKDNS_TOKEN="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}[ERROR] Unknown option: $1${NC}" >&2
            usage
            ;;
    esac
done

echo -e "${CYAN}======================================================================${NC}"
echo -e "${CYAN}${BOLD}   GYM MANAGEMENT - EMERGENCY QUICK RESTART & RECOVERY RUNBOOK   ${NC}"
echo -e "${CYAN}======================================================================${NC}"
echo -e "Environment : ${BOLD}${ENVIRONMENT}${NC}"
echo -e "AWS Region  : ${BOLD}${AWS_REGION}${NC}"
echo -e "ECS Cluster : ${BOLD}${ECS_CLUSTER}${NC}"
echo -e "RDS DB ID   : ${BOLD}${RDS_IDENTIFIER}${NC}"
echo -e "Dry Run     : ${BOLD}$([ "$DRY_RUN" = true ] && echo "YES (Simulated)" || echo "NO (Live Execution)")${NC}"
echo -e "${CYAN}----------------------------------------------------------------------${NC}\n"

START_TIME=$(date +%s)

# ==============================================================================
# STEP 1: CHECK & WAKE RDS MYSQL DATABASE
# ==============================================================================
echo -e "${BOLD}[STEP 1/3] Checking RDS MySQL Instance State...${NC}"
if [ "$SKIP_RDS" = true ]; then
    echo -e "${YELLOW}  [SKIPPED] RDS check bypassed via --skip-rds flag.${NC}\n"
else
    if [ "$DRY_RUN" = true ]; then
        echo -e "  [DRY-RUN] Would query RDS status for [${RDS_IDENTIFIER}]"
        echo -e "  [DRY-RUN] If stopped, would execute: aws rds start-db-instance --db-instance-identifier ${RDS_IDENTIFIER}"
        echo -e "  [DRY-RUN] Would wait for status: db-instance-available\n"
    else
        RDS_STATUS=$(aws rds describe-db-instances \
            --db-instance-identifier "$RDS_IDENTIFIER" \
            --region "$AWS_REGION" \
            --query "DBInstances[0].DBInstanceStatus" \
            --output text 2>/dev/null || echo "NOT_FOUND")

        echo -e "  Current RDS Status: ${BOLD}${RDS_STATUS}${NC}"

        if [ "$RDS_STATUS" = "available" ]; then
            echo -e "  ${GREEN}✓ RDS instance is already available and ready.${NC}\n"
        elif [ "$RDS_STATUS" = "stopped" ] || [ "$RDS_STATUS" = "starting" ]; then
            if [ "$RDS_STATUS" = "stopped" ]; then
                echo -e "  ${YELLOW}⚡ RDS instance is stopped. Triggering start-db-instance...${NC}"
                aws rds start-db-instance \
                    --db-instance-identifier "$RDS_IDENTIFIER" \
                    --region "$AWS_REGION" > /dev/null
            fi
            
            echo -e "  Waiting for RDS instance to become available (db-instance-available)..."
            aws rds wait db-instance-available \
                --db-instance-identifier "$RDS_IDENTIFIER" \
                --region "$AWS_REGION"
            echo -e "  ${GREEN}✓ RDS instance is now available!${NC}\n"
        elif [ "$RDS_STATUS" = "NOT_FOUND" ]; then
            echo -e "  ${YELLOW}⚠ RDS instance [${RDS_IDENTIFIER}] not found or access denied. Skipping RDS wake.${NC}\n"
        else
            echo -e "  ${YELLOW}ℹ RDS instance state: ${RDS_STATUS}. Proceeding with rolling deployment.${NC}\n"
        fi
    fi
fi

# ==============================================================================
# STEP 2: ROLLING FORCE-NEW-DEPLOYMENT ON ECS FARGATE
# ==============================================================================
echo -e "${BOLD}[STEP 2/3] Triggering Zero-Downtime Rolling Update on ECS Services...${NC}"
for SVC in "$BACKEND_SERVICE" "$FRONTEND_SERVICE"; do
    if [ "$DRY_RUN" = true ]; then
        echo -e "  [DRY-RUN] Would trigger rolling update: aws ecs update-service --cluster ${ECS_CLUSTER} --service ${SVC} --force-new-deployment"
    else
        echo -e "  Executing force update on [${SVC}]..."
        UPDATE_RES=$(aws ecs update-service \
            --cluster "$ECS_CLUSTER" \
            --service "$SVC" \
            --force-new-deployment \
            --region "$AWS_REGION" \
            --query "service.[serviceName,status,desiredCount,runningCount]" \
            --output text 2>/dev/null || echo "FAILED")

        if [ "$UPDATE_RES" != "FAILED" ]; then
            echo -e "  ${GREEN}✓ Force update initiated successfully for ${SVC}.${NC}"
        else
            echo -e "  ${RED}✗ Failed to update service ${SVC}. Please check IAM permissions.${NC}"
        fi
    fi
done
echo ""

# ==============================================================================
# STEP 3: DUCKDNS DYNAMIC DNS SYNCHRONIZATION (WITH CHANGE DETECTION)
# ==============================================================================
echo -e "${BOLD}[STEP 3/3] Dynamic DNS Re-sync (DuckDNS)...${NC}"
if [ "$SKIP_DNS" = true ]; then
    echo -e "${YELLOW}  [SKIPPED] DuckDNS sync bypassed via --skip-dns flag.${NC}\n"
else
    if [ "$DRY_RUN" = true ]; then
        echo -e "  [DRY-RUN] Would resolve ALB DNS for [${ALB_NAME}]"
        echo -e "  [DRY-RUN] Would check if current DuckDNS IP differs from ALB IP"
        echo -e "  [DRY-RUN] If changed, would call DuckDNS update API: https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=***&ip=<ALB_IP>\n"
    else
        ALB_DNS=$(aws elbv2 describe-load-balancers \
            --names "$ALB_NAME" \
            --region "$AWS_REGION" \
            --query "LoadBalancers[0].DNSName" \
            --output text 2>/dev/null || echo "")

        if [ -n "$ALB_DNS" ] && [ "$ALB_DNS" != "None" ]; then
            echo -e "  Found ALB DNS: ${ALB_DNS}"
            ALB_IP=""
            for i in {1..5}; do
                ALB_IP=$(nslookup "$ALB_DNS" 2>/dev/null | grep -A 2 "Name:" | grep "Address:" | awk '{print $2}' | head -n1 || true)
                if [ -z "$ALB_IP" ]; then
                    ALB_IP=$(getent hosts "$ALB_DNS" 2>/dev/null | awk '{print $1}' | head -n1 || true)
                fi
                if [ -n "$ALB_IP" ]; then break; fi
                sleep 2
            done

            if [ -n "$ALB_IP" ]; then
                echo -e "  Resolved ALB Public IPv4: ${BOLD}${ALB_IP}${NC}"
                
                # Check current DuckDNS IP to detect if re-sync is required
                CURRENT_DUCKDNS_IP=$(nslookup "${DUCKDNS_DOMAIN}.duckdns.org" 2>/dev/null | grep -A 2 "Name:" | grep "Address:" | awk '{print $2}' | head -n1 || true)
                if [ -z "$CURRENT_DUCKDNS_IP" ]; then
                    CURRENT_DUCKDNS_IP=$(getent hosts "${DUCKDNS_DOMAIN}.duckdns.org" 2>/dev/null | awk '{print $1}' | head -n1 || true)
                fi

                if [ -n "$CURRENT_DUCKDNS_IP" ] && [ "$CURRENT_DUCKDNS_IP" = "$ALB_IP" ]; then
                    echo -e "  ${GREEN}✓ DuckDNS IP (${CURRENT_DUCKDNS_IP}) is already synchronized with ALB IP. No update needed.${NC}"
                else
                    echo -e "  DuckDNS IP change detected (Current: ${CURRENT_DUCKDNS_IP:-Unresolved} -> ALB: ${ALB_IP}). Re-syncing..."
                    if [ -n "$DUCKDNS_TOKEN" ]; then
                        DDNS_RESP=$(curl -s "https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=${DUCKDNS_TOKEN}&ip=${ALB_IP}" || echo "ERROR")
                        if [ "$DDNS_RESP" = "OK" ]; then
                            echo -e "  ${GREEN}✓ DuckDNS synchronized successfully (OK).${NC}"
                        else
                            echo -e "  ${YELLOW}⚠ DuckDNS API returned: ${DDNS_RESP}${NC}"
                        fi
                    else
                        echo -e "  ${YELLOW}ℹ DUCKDNS_TOKEN not provided. Skipping DuckDNS API call.${NC}"
                        echo -e "    Manual sync URL: https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=<TOKEN>&ip=${ALB_IP}"
                    fi
                fi
            else
                echo -e "  ${YELLOW}⚠ Could not resolve IPv4 address for ALB DNS [${ALB_DNS}].${NC}"
            fi
        else
            echo -e "  ${YELLOW}⚠ ALB [${ALB_NAME}] not found or access denied.${NC}"
        fi
    fi
    echo ""
fi

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "${CYAN}======================================================================${NC}"
if [ "$ELAPSED" -le 60 ]; then
    echo -e "${GREEN}${BOLD}   EMERGENCY RESTART DISPATCH COMPLETED IN ${ELAPSED}s (WITHIN 60s TARGET)   ${NC}"
else
    echo -e "${YELLOW}${BOLD}   EMERGENCY RESTART COMPLETED IN ${ELAPSED}s (> 60s DUE TO RDS WAKE TIME)    ${NC}"
fi
echo -e "${CYAN}======================================================================${NC}"
echo -e "Next steps:"
echo -e "  1. Run multi-tier health check: ./scripts/check-ha-health.sh --http-only"
echo -e "  2. Tail backend CloudWatch logs: aws logs tail /ecs/gym-${ENVIRONMENT}-backend --follow"
echo -e "  3. Open Live URL: http://${DUCKDNS_DOMAIN}.duckdns.org\n"
exit 0
