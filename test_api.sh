#!/bin/bash

echo "=== Testing HuSpark Authentication API ==="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${YELLOW}1. Testing User Registration${NC}"
echo "POST $BASE_URL/api/auth/register"

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com", 
    "password": "password123"
  }')

echo "Response: $REGISTER_RESPONSE"
echo

# Extract token from registration response (if successful)
TOKEN=$(echo "$REGISTER_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}Registration successful! Token: ${TOKEN:0:20}...${NC}"
else
    echo -e "${RED}Registration failed or token not found${NC}"
fi

echo
echo -e "${YELLOW}2. Testing User Login${NC}"
echo "POST $BASE_URL/api/auth/login"

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
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
echo -e "${YELLOW}3. Testing Protected Endpoint - User Profile${NC}"
echo "GET $BASE_URL/api/users/profile"

if [ -n "$TOKEN" ]; then
    PROFILE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/profile" \
      -H "Authorization: Bearer $TOKEN")
    echo "Response: $PROFILE_RESPONSE"
else
    echo -e "${RED}No token available, skipping protected endpoint tests${NC}"
fi

echo
echo -e "${YELLOW}4. Testing Protected Endpoint - Member or Above${NC}" 
echo "GET $BASE_URL/api/users/member-or-above"

if [ -n "$TOKEN" ]; then
    MEMBER_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/member-or-above" \
      -H "Authorization: Bearer $TOKEN")
    echo "Response: $MEMBER_RESPONSE"
else
    echo -e "${RED}No token available, skipping protected endpoint tests${NC}"
fi

echo
echo -e "${YELLOW}5. Testing Admin-Only Endpoint (Should fail for regular user)${NC}"
echo "GET $BASE_URL/api/users/admin-only"

if [ -n "$TOKEN" ]; then
    ADMIN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/admin-only" \
      -H "Authorization: Bearer $TOKEN")
    echo "Response: $ADMIN_RESPONSE"
else
    echo -e "${RED}No token available, skipping protected endpoint tests${NC}"
fi

echo
echo -e "${YELLOW}6. Testing Endpoint Without Token (Should fail)${NC}"
echo "GET $BASE_URL/api/users/profile (without token)"

NO_TOKEN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/profile")
echo "Response: $NO_TOKEN_RESPONSE"

echo
echo -e "${GREEN}=== Test Complete ===${NC}"
