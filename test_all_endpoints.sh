#!/bin/bash

echo "=== Testing All HuSpark API Endpoints ==="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}=== 1. Testing Public Endpoints ===${NC}"
echo

echo -e "${YELLOW}1.1 Test endpoint (public)${NC}"
echo "GET $BASE_URL/test/hello"
TEST_RESPONSE=$(curl -s -X GET "$BASE_URL/test/hello")
echo "Response: $TEST_RESPONSE"
echo

echo -e "${BLUE}=== 2. Testing Authentication Endpoints ===${NC}"
echo

echo -e "${YELLOW}2.1 User Registration${NC}"
echo "POST $BASE_URL/api/auth/register"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test.user@example.com", 
    "password": "password123"
  }')

echo "Response: $REGISTER_RESPONSE"

# Extract token from registration response
TOKEN=$(echo "$REGISTER_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}Registration successful! Token: ${TOKEN:0:20}...${NC}"
else
    echo -e "${RED}Registration failed or token not found${NC}"
fi
echo

echo -e "${YELLOW}2.2 User Login${NC}"
echo "POST $BASE_URL/api/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "password123"
  }')

echo "Response: $LOGIN_RESPONSE"

# Extract token from login response
LOGIN_TOKEN=$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

if [ -n "$LOGIN_TOKEN" ]; then
    echo -e "${GREEN}Login successful! Token: ${LOGIN_TOKEN:0:20}...${NC}"
    TOKEN="$LOGIN_TOKEN"  # Use login token for subsequent requests
else
    echo -e "${RED}Login failed or token not found${NC}"
fi
echo

echo -e "${BLUE}=== 3. Testing Protected User Endpoints ===${NC}"
echo

if [ -n "$TOKEN" ]; then
    echo -e "${YELLOW}3.1 Get User Profile (authenticated)${NC}"
    echo "GET $BASE_URL/api/users/profile"
    PROFILE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/profile" \
      -H "Authorization: Bearer $TOKEN")
    echo "Response: $PROFILE_RESPONSE"
    echo

    echo -e "${YELLOW}3.2 Member or Above Endpoint (authenticated)${NC}"
    echo "GET $BASE_URL/api/users/member-or-above"
    MEMBER_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/member-or-above" \
      -H "Authorization: Bearer $TOKEN")
    echo "Response: $MEMBER_RESPONSE"
    echo

    echo -e "${YELLOW}3.3 Admin Only Endpoint (should fail for regular user)${NC}"
    echo "GET $BASE_URL/api/users/admin-only"
    ADMIN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/admin-only" \
      -H "Authorization: Bearer $TOKEN")
    echo "Response: $ADMIN_RESPONSE"
    echo
else
    echo -e "${RED}No token available, skipping protected endpoint tests${NC}"
    echo
fi

echo -e "${BLUE}=== 4. Testing Error Scenarios ===${NC}"
echo

echo -e "${YELLOW}4.1 Access Protected Endpoint Without Token${NC}"
echo "GET $BASE_URL/api/users/profile (without token)"
NO_TOKEN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/profile")
echo "Response: $NO_TOKEN_RESPONSE"
echo

echo -e "${YELLOW}4.2 Login with Wrong Password${NC}"
echo "POST $BASE_URL/api/auth/login (wrong password)"
WRONG_PASSWORD_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "wrongpassword"
  }')
echo "Response: $WRONG_PASSWORD_RESPONSE"
echo

echo -e "${YELLOW}4.3 Register with Invalid Email${NC}"
echo "POST $BASE_URL/api/auth/register (invalid email)"
INVALID_EMAIL_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Invalid User",
    "email": "invalid-email", 
    "password": "password123"
  }')
echo "Response: $INVALID_EMAIL_RESPONSE"
echo

echo -e "${YELLOW}4.4 Register with Missing Fields${NC}"
echo "POST $BASE_URL/api/auth/register (missing fields)"
MISSING_FIELDS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Incomplete User"
  }')
echo "Response: $MISSING_FIELDS_RESPONSE"
echo

echo -e "${YELLOW}4.5 Register with Duplicate Email${NC}"
echo "POST $BASE_URL/api/auth/register (duplicate email)"
DUPLICATE_EMAIL_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Duplicate User",
    "email": "test.user@example.com", 
    "password": "password123"
  }')
echo "Response: $DUPLICATE_EMAIL_RESPONSE"
echo

echo -e "${GREEN}=== All Endpoint Tests Complete ===${NC}"
