#!/bin/bash
# Health monitoring script for deployment validation
# Checks application health, response times, and basic metrics

set -e

# Configuration
APP_URL="${APP_URL:-http://localhost:8080}"
HEALTH_ENDPOINT="${HEALTH_ENDPOINT:-/actuator/health}"
MAX_RETRIES=30
RETRY_INTERVAL=10
TIMEOUT=5

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🏥 Health Monitor Starting..."
echo "Target: $APP_URL"
echo "========================================"

# Function to check health endpoint
check_health() {
    local url="$1"
    local response
    local http_code
    local response_time

    # Measure response time and get HTTP code
    response=$(curl -s -w "\n%{http_code}\n%{time_total}" --max-time "$TIMEOUT" "$url" 2>/dev/null || echo -e "\n000\n0")
    http_code=$(echo "$response" | tail -n 2 | head -n 1)
    response_time=$(echo "$response" | tail -n 1)

    echo "$http_code|$response_time"
}

# Function to wait for application to be ready
wait_for_health() {
    local retry_count=0

    echo "⏳ Waiting for application to be healthy..."

    while [ $retry_count -lt $MAX_RETRIES ]; do
        result=$(check_health "$APP_URL$HEALTH_ENDPOINT")
        http_code=$(echo "$result" | cut -d'|' -f1)
        response_time=$(echo "$result" | cut -d'|' -f2)

        if [ "$http_code" == "200" ]; then
            echo -e "${GREEN}✅ Application is healthy!${NC}"
            echo "   HTTP Code: $http_code"
            echo "   Response Time: ${response_time}s"
            return 0
        else
            retry_count=$((retry_count + 1))
            echo "   Attempt $retry_count/$MAX_RETRIES - Status: $http_code (retrying in ${RETRY_INTERVAL}s...)"
            sleep $RETRY_INTERVAL
        fi
    done

    echo -e "${RED}❌ Application failed to become healthy after $MAX_RETRIES attempts${NC}"
    return 1
}

# Function to check performance metrics
check_performance() {
    echo ""
    echo "📊 Checking performance metrics..."

    result=$(check_health "$APP_URL$HEALTH_ENDPOINT")
    http_code=$(echo "$result" | cut -d'|' -f1)
    response_time=$(echo "$result" | cut -d'|' -f2)

    # Convert response time to milliseconds for easier comparison
    response_time_ms=$(echo "$response_time * 1000" | bc)
    response_time_ms=${response_time_ms%.*}

    echo "   Response Time: ${response_time_ms}ms"

    # Check if response time is acceptable (< 1000ms for health check)
    if [ "$response_time_ms" -lt 1000 ]; then
        echo -e "   ${GREEN}✅ Response time is good${NC}"
    elif [ "$response_time_ms" -lt 3000 ]; then
        echo -e "   ${YELLOW}⚠️  Response time is acceptable but could be better${NC}"
    else
        echo -e "   ${RED}❌ Response time is too slow${NC}"
        return 1
    fi
}

# Function to test critical endpoints
test_endpoints() {
    echo ""
    echo "🔍 Testing critical endpoints..."

    # Test root endpoint
    result=$(check_health "$APP_URL/")
    http_code=$(echo "$result" | cut -d'|' -f1)

    if [ "$http_code" == "200" ] || [ "$http_code" == "302" ] || [ "$http_code" == "404" ]; then
        echo -e "   ${GREEN}✅ Root endpoint responding${NC}"
    else
        echo -e "   ${RED}❌ Root endpoint not responding properly (HTTP $http_code)${NC}"
    fi

    # Test API endpoint (if exists)
    result=$(check_health "$APP_URL/api/tasks")
    http_code=$(echo "$result" | cut -d'|' -f1)

    if [ "$http_code" == "200" ]; then
        echo -e "   ${GREEN}✅ API endpoint healthy${NC}"
    else
        echo -e "   ${YELLOW}⚠️  API endpoint returned HTTP $http_code${NC}"
    fi
}

# Function to collect deployment metrics
collect_metrics() {
    echo ""
    echo "📈 Collecting deployment metrics..."

    local start_time=$(date +%s)

    # Wait for health
    if wait_for_health; then
        local end_time=$(date +%s)
        local startup_time=$((end_time - start_time))

        echo ""
        echo "========================================"
        echo "📊 DEPLOYMENT METRICS:"
        echo "   Startup Time: ${startup_time}s"

        # Check if startup time meets requirements (< 5 min = 300s)
        if [ "$startup_time" -lt 300 ]; then
            echo -e "   ${GREEN}✅ Deployment time requirement met (< 5 min)${NC}"
        else
            echo -e "   ${YELLOW}⚠️  Deployment took longer than expected${NC}"
        fi

        # Run performance checks
        check_performance

        # Test endpoints
        test_endpoints

        echo "========================================"
        echo -e "${GREEN}✅ Health monitoring completed successfully${NC}"
        return 0
    else
        echo "========================================"
        echo -e "${RED}❌ Health monitoring failed${NC}"
        return 1
    fi
}

# Main execution
main() {
    collect_metrics
}

main
