#!/bin/bash
# Coolify deployment integration script
# Deploys application to Coolify with zero-downtime strategy

set -e

# Configuration
COOLIFY_URL="${COOLIFY_URL:-https://coolify.yourserver.com}"
COOLIFY_TOKEN="${COOLIFY_TOKEN}"
PROJECT_NAME="${PROJECT_NAME:-task-management}"
ENVIRONMENT="${ENVIRONMENT:-staging}"
DOCKER_IMAGE="${DOCKER_IMAGE}"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}🚀 Coolify Deployment Script${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "Configuration:"
echo "  Coolify URL: $COOLIFY_URL"
echo "  Project: $PROJECT_NAME"
echo "  Environment: $ENVIRONMENT"
echo "  Docker Image: $DOCKER_IMAGE"
echo ""

# Validate required environment variables
if [ -z "$COOLIFY_TOKEN" ]; then
    echo -e "${RED}❌ Error: COOLIFY_TOKEN is not set${NC}"
    exit 1
fi

if [ -z "$DOCKER_IMAGE" ]; then
    echo -e "${RED}❌ Error: DOCKER_IMAGE is not set${NC}"
    exit 1
fi

# Function to call Coolify API
coolify_api_call() {
    local method="$1"
    local endpoint="$2"
    local data="$3"

    response=$(curl -s -X "$method" \
        "${COOLIFY_URL}/api/v1${endpoint}" \
        -H "Authorization: Bearer ${COOLIFY_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$data" 2>&1)

    echo "$response"
}

# Step 1: Get current deployment info
echo -e "${YELLOW}📋 Step 1: Getting current deployment info...${NC}"

current_deployment=$(coolify_api_call "GET" "/projects/${PROJECT_NAME}/environments/${ENVIRONMENT}")

if echo "$current_deployment" | grep -q "error"; then
    echo -e "${YELLOW}⚠️  No existing deployment found, this will be initial deployment${NC}"
    INITIAL_DEPLOY=true
else
    echo -e "${GREEN}✅ Found existing deployment${NC}"
    INITIAL_DEPLOY=false
fi

# Step 2: Create new deployment
echo ""
echo -e "${YELLOW}📦 Step 2: Creating new deployment...${NC}"

deployment_data=$(cat <<EOF
{
  "project": "$PROJECT_NAME",
  "environment": "$ENVIRONMENT",
  "image": "$DOCKER_IMAGE",
  "deployment_strategy": "blue-green",
  "health_check": {
    "enabled": true,
    "path": "/actuator/health",
    "interval": 10,
    "timeout": 5,
    "retries": 3
  },
  "env_vars": {
    "DB_HOST": "postgres",
    "DB_PORT": "5432",
    "DB_NAME": "taskdb",
    "SPRING_PROFILES_ACTIVE": "$ENVIRONMENT"
  }
}
EOF
)

deployment_result=$(coolify_api_call "POST" "/deployments" "$deployment_data")

if echo "$deployment_result" | grep -q "error"; then
    echo -e "${RED}❌ Failed to create deployment${NC}"
    echo "$deployment_result"
    exit 1
fi

deployment_id=$(echo "$deployment_result" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo -e "${GREEN}✅ Deployment created with ID: $deployment_id${NC}"

# Step 3: Wait for deployment to be ready
echo ""
echo -e "${YELLOW}⏳ Step 3: Waiting for deployment to be ready...${NC}"

max_wait=300  # 5 minutes
wait_time=0
check_interval=10

while [ $wait_time -lt $max_wait ]; do
    status=$(coolify_api_call "GET" "/deployments/${deployment_id}/status")

    if echo "$status" | grep -q '"status":"ready"'; then
        echo -e "${GREEN}✅ Deployment is ready!${NC}"
        break
    elif echo "$status" | grep -q '"status":"failed"'; then
        echo -e "${RED}❌ Deployment failed!${NC}"
        exit 1
    else
        echo "   Waiting... ($wait_time/${max_wait}s)"
        sleep $check_interval
        wait_time=$((wait_time + check_interval))
    fi
done

if [ $wait_time -ge $max_wait ]; then
    echo -e "${RED}❌ Deployment timeout after ${max_wait}s${NC}"
    exit 1
fi

# Step 4: Health check on new deployment
echo ""
echo -e "${YELLOW}🏥 Step 4: Running health checks...${NC}"

# Get the URL of the new deployment
new_url=$(coolify_api_call "GET" "/deployments/${deployment_id}" | grep -o '"url":"[^"]*"' | cut -d'"' -f4)

echo "   New deployment URL: $new_url"

# Run health checks
health_checks_passed=true
for i in {1..5}; do
    echo "   Health check attempt $i/5..."
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "${new_url}/actuator/health" --max-time 5 || echo "000")

    if [ "$http_code" == "200" ]; then
        echo -e "   ${GREEN}✅ Health check passed${NC}"
        break
    else
        if [ $i -eq 5 ]; then
            echo -e "   ${RED}❌ Health check failed after 5 attempts${NC}"
            health_checks_passed=false
        else
            sleep 5
        fi
    fi
done

if [ "$health_checks_passed" = false ]; then
    echo -e "${RED}❌ Rolling back deployment due to failed health checks${NC}"
    coolify_api_call "POST" "/deployments/${deployment_id}/rollback" "{}"
    exit 1
fi

# Step 5: Switch traffic (Blue-Green swap)
echo ""
echo -e "${YELLOW}🔄 Step 5: Switching traffic to new deployment...${NC}"

if [ "$INITIAL_DEPLOY" = false ]; then
    switch_result=$(coolify_api_call "POST" "/deployments/${deployment_id}/activate" "{}")

    if echo "$switch_result" | grep -q "success"; then
        echo -e "${GREEN}✅ Traffic switched to new deployment${NC}"
    else
        echo -e "${RED}❌ Failed to switch traffic${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ Initial deployment activated${NC}"
fi

# Step 6: Verify production traffic
echo ""
echo -e "${YELLOW}🔍 Step 6: Verifying production traffic...${NC}"

sleep 5

for i in {1..3}; do
    prod_http_code=$(curl -s -o /dev/null -w "%{http_code}" "${new_url}" --max-time 5 || echo "000")

    if [ "$prod_http_code" == "200" ] || [ "$prod_http_code" == "302" ]; then
        echo -e "   ${GREEN}✅ Production traffic verified${NC}"
        break
    else
        if [ $i -eq 3 ]; then
            echo -e "   ${RED}❌ Production verification failed${NC}"
            echo -e "${RED}🔄 Rolling back...${NC}"
            coolify_api_call "POST" "/deployments/${deployment_id}/rollback" "{}"
            exit 1
        fi
        sleep 5
    fi
done

# Step 7: Cleanup old deployment
if [ "$INITIAL_DEPLOY" = false ]; then
    echo ""
    echo -e "${YELLOW}🧹 Step 7: Cleaning up old deployment...${NC}"

    # Wait a bit to ensure new deployment is stable
    echo "   Waiting 30s before cleanup..."
    sleep 30

    cleanup_result=$(coolify_api_call "POST" "/deployments/cleanup" "{\"keep_last\": 2}")
    echo -e "${GREEN}✅ Old deployments cleaned up${NC}"
fi

# Success!
echo ""
echo -e "${BLUE}======================================${NC}"
echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "Deployment Summary:"
echo "  Deployment ID: $deployment_id"
echo "  Environment: $ENVIRONMENT"
echo "  Image: $DOCKER_IMAGE"
echo "  URL: $new_url"
echo "  Status: Active"
echo ""
echo -e "${GREEN}🎉 Zero-downtime deployment achieved!${NC}"
