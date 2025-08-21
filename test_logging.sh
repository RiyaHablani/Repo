#!/bin/bash

# Test script for logging functionality in UserService
echo "=== Testing Logging Functionality in UserService ==="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8080"

# Test user credentials
TEST_EMAIL="testuser@example.com"
TEST_PASSWORD="testpassword123"
TEST_NAME="Test User"

echo -e "${YELLOW}Starting logging tests...${NC}"

# Function to check if application is running
check_app_running() {
    echo "Checking if application is running..."
    if curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${GREEN}✓ Application is running${NC}"
        return 0
    else
        echo -e "${RED}✗ Application is not running. Please start the application first.${NC}"
        return 1
    fi
}

# Function to test user registration logging
test_registration_logging() {
    echo -e "\n${YELLOW}Testing User Registration Logging...${NC}"
    
    # Register a new user
    echo "Registering user: $TEST_EMAIL"
    REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$TEST_NAME\",
            \"email\": \"$TEST_EMAIL\",
            \"password\": \"$TEST_PASSWORD\"
        }")
    
    if echo "$REGISTER_RESPONSE" | grep -q "token"; then
        echo -e "${GREEN}✓ User registration successful${NC}"
        echo "Response: $REGISTER_RESPONSE"
    else
        echo -e "${RED}✗ User registration failed${NC}"
        echo "Response: $REGISTER_RESPONSE"
    fi
}

# Function to test user login logging
test_login_logging() {
    echo -e "\n${YELLOW}Testing User Login Logging...${NC}"
    
    # Login with the user
    echo "Logging in user: $TEST_EMAIL"
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$TEST_EMAIL\",
            \"password\": \"$TEST_PASSWORD\"
        }")
    
    if echo "$LOGIN_RESPONSE" | grep -q "token"; then
        echo -e "${GREEN}✓ User login successful${NC}"
        echo "Response: $LOGIN_RESPONSE"
        
        # Extract token for further tests
        TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        echo "Token extracted: ${TOKEN:0:20}..."
    else
        echo -e "${RED}✗ User login failed${NC}"
        echo "Response: $LOGIN_RESPONSE"
    fi
}

# Function to test failed login logging
test_failed_login_logging() {
    echo -e "\n${YELLOW}Testing Failed Login Logging...${NC}"
    
    # Try to login with wrong password
    echo "Attempting login with wrong password for: $TEST_EMAIL"
    FAILED_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$TEST_EMAIL\",
            \"password\": \"wrongpassword\"
        }")
    
    if echo "$FAILED_LOGIN_RESPONSE" | grep -q "error"; then
        echo -e "${GREEN}✓ Failed login properly handled${NC}"
        echo "Response: $FAILED_LOGIN_RESPONSE"
    else
        echo -e "${RED}✗ Failed login not properly handled${NC}"
        echo "Response: $FAILED_LOGIN_RESPONSE"
    fi
}

# Function to test duplicate registration logging
test_duplicate_registration_logging() {
    echo -e "\n${YELLOW}Testing Duplicate Registration Logging...${NC}"
    
    # Try to register the same user again
    echo "Attempting to register duplicate user: $TEST_EMAIL"
    DUPLICATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$TEST_NAME\",
            \"email\": \"$TEST_EMAIL\",
            \"password\": \"$TEST_PASSWORD\"
        }")
    
    if echo "$DUPLICATE_RESPONSE" | grep -q "already exists"; then
        echo -e "${GREEN}✓ Duplicate registration properly handled${NC}"
        echo "Response: $DUPLICATE_RESPONSE"
    else
        echo -e "${RED}✗ Duplicate registration not properly handled${NC}"
        echo "Response: $DUPLICATE_RESPONSE"
    fi
}

# Function to check log files
check_log_files() {
    echo -e "\n${YELLOW}Checking Log Files...${NC}"
    
    # Check if logs directory exists
    if [ -d "logs" ]; then
        echo -e "${GREEN}✓ Logs directory exists${NC}"
        
        # List log files
        echo "Log files found:"
        ls -la logs/
        
        # Check the latest log file
        LATEST_LOG=$(ls -t logs/*.log 2>/dev/null | head -1)
        if [ -n "$LATEST_LOG" ]; then
            echo -e "\n${GREEN}✓ Latest log file: $LATEST_LOG${NC}"
            
            # Show last 20 lines of the log file
            echo -e "\n${YELLOW}Last 20 lines of log file:${NC}"
            tail -20 "$LATEST_LOG"
        else
            echo -e "${RED}✗ No log files found${NC}"
        fi
    else
        echo -e "${RED}✗ Logs directory not found${NC}"
    fi
}

# Function to test actuator endpoints
test_actuator_endpoints() {
    echo -e "\n${YELLOW}Testing Actuator Endpoints...${NC}"
    
    # Test health endpoint
    echo "Testing health endpoint..."
    HEALTH_RESPONSE=$(curl -s "$BASE_URL/actuator/health")
    if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
        echo -e "${GREEN}✓ Health endpoint working${NC}"
    else
        echo -e "${RED}✗ Health endpoint not working${NC}"
    fi
    
    # Test info endpoint
    echo "Testing info endpoint..."
    INFO_RESPONSE=$(curl -s "$BASE_URL/actuator/info")
    if [ -n "$INFO_RESPONSE" ]; then
        echo -e "${GREEN}✓ Info endpoint working${NC}"
    else
        echo -e "${RED}✗ Info endpoint not working${NC}"
    fi
    
    # Test metrics endpoint
    echo "Testing metrics endpoint..."
    METRICS_RESPONSE=$(curl -s "$BASE_URL/actuator/metrics")
    if echo "$METRICS_RESPONSE" | grep -q "names"; then
        echo -e "${GREEN}✓ Metrics endpoint working${NC}"
    else
        echo -e "${RED}✗ Metrics endpoint not working${NC}"
    fi
}

# Function to test reporting endpoints
test_reporting_endpoints() {
    echo -e "\n${YELLOW}Testing Reporting Endpoints...${NC}"
    
    # Get token for authenticated requests
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$TEST_EMAIL\",
            \"password\": \"$TEST_PASSWORD\"
        }")
    
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$TOKEN" ]; then
        echo "Testing reporting endpoints with authentication..."
        
        # Test statistics endpoint
        echo "Testing statistics endpoint..."
        STATS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/reports/statistics" \
            -H "Authorization: Bearer $TOKEN")
        
        if echo "$STATS_RESPONSE" | grep -q "totalBorrowings"; then
            echo -e "${GREEN}✓ Statistics endpoint working${NC}"
        else
            echo -e "${RED}✗ Statistics endpoint not working${NC}"
        fi
        
        # Test overdue count endpoint
        echo "Testing overdue count endpoint..."
        OVERDUE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/reports/overdue-count" \
            -H "Authorization: Bearer $TOKEN")
        
        if echo "$OVERDUE_RESPONSE" | grep -q "overdueCount"; then
            echo -e "${GREEN}✓ Overdue count endpoint working${NC}"
        else
            echo -e "${RED}✗ Overdue count endpoint not working${NC}"
        fi
    else
        echo -e "${RED}✗ Could not get authentication token for reporting tests${NC}"
    fi
}

# Main test execution
main() {
    echo "=== UserService Logging Test Suite ==="
    echo "This script will test the logging functionality implemented in UserService"
    echo "Make sure the application is running on $BASE_URL"
    echo ""
    
    # Check if application is running
    if ! check_app_running; then
        exit 1
    fi
    
    # Run all tests
    test_registration_logging
    test_login_logging
    test_failed_login_logging
    test_duplicate_registration_logging
    test_actuator_endpoints
    test_reporting_endpoints
    check_log_files
    
    echo -e "\n${GREEN}=== Logging Test Complete ===${NC}"
    echo "Check the log files in the 'logs' directory to verify structured logging is working."
    echo "Look for JSON-formatted log entries with masked sensitive data."
}

# Run the main function
main
