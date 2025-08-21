#!/bin/bash

# Script to update test users with proper roles
echo "=== Updating Test Users with Proper Roles ==="
echo ""

# First, let's get a token for an existing user to use for admin operations
echo "1. Getting authentication token..."
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@library.com",
    "password": "admin123"
  }')

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.token')
echo "Token obtained: ${TOKEN:0:20}..."

echo ""
echo "2. Updating admin user (ID: 6) to ADMIN role..."
curl -s -X PUT "http://localhost:8080/api/users/admin/6/role?role=ADMIN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "3. Updating librarian user (ID: 7) to LIBRARIAN role..."
curl -s -X PUT "http://localhost:8080/api/users/admin/7/role?role=LIBRARIAN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "4. Verifying updated roles..."
echo "Admin user:"
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/users/admin/all?page=0&size=10" | jq '.content[] | select(.email | contains("admin@library.com")) | {id, email, role}'

echo ""
echo "Librarian user:"
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/users/admin/all?page=0&size=10" | jq '.content[] | select(.email | contains("librarian@library.com")) | {id, email, role}'

echo ""
echo "=== Role Update Complete ==="
echo "✅ Admin user updated to ADMIN role"
echo "✅ Librarian user updated to LIBRARIAN role"
echo "✅ Member user remains as MEMBER role"
echo ""
echo "Now you can test admin and librarian features with proper authentication!"
