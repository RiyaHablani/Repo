#!/bin/bash

echo "=== Testing HuSpark Book Catalog with RBAC ==="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

# Function to get token
get_token() {
    local email=$1
    local password=$2
    
    local response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
      -H "Content-Type: application/json" \
      -d "{
        \"email\": \"$email\",
        \"password\": \"$password\"
      }")
    
    echo "$response" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'
}

echo -e "${BLUE}=== 1. Setting up Test Users ===${NC}"
echo

# Create test users with different roles
echo -e "${YELLOW}1.1 Creating Member User${NC}"
MEMBER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Member",
    "email": "member@test.com", 
    "password": "password123"
  }')
echo "Response: $MEMBER_RESPONSE"
MEMBER_TOKEN=$(get_token "member@test.com" "password123")
echo -e "${GREEN}Member Token: ${MEMBER_TOKEN:0:20}...${NC}"
echo

echo -e "${YELLOW}1.2 Creating Librarian User${NC}"
LIBRARIAN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Librarian",
    "email": "librarian@test.com", 
    "password": "password123"
  }')
echo "Response: $LIBRARIAN_RESPONSE"
LIBRARIAN_TOKEN=$(get_token "librarian@test.com" "password123")
echo -e "${GREEN}Librarian Token: ${LIBRARIAN_TOKEN:0:20}...${NC}"
echo

echo -e "${BLUE}=== 2. Testing Book Catalog Operations ===${NC}"
echo

echo -e "${YELLOW}2.1 Testing Member Access - View All Books${NC}"
echo "GET $BASE_URL/api/books"
MEMBER_BOOKS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_BOOKS_RESPONSE"
echo

echo -e "${YELLOW}2.2 Testing Member Access - Search Books${NC}"
echo "GET $BASE_URL/api/books/search?q=test"
MEMBER_SEARCH_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/search?q=test" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_SEARCH_RESPONSE"
echo

echo -e "${YELLOW}2.3 Testing Member Access - Create Book (Should Fail)${NC}"
echo "POST $BASE_URL/api/books"
MEMBER_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/books" \
  -H "Authorization: Bearer $MEMBER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Book",
    "author": "Test Author",
    "barcode": "123456789"
  }')
echo "Response: $MEMBER_CREATE_RESPONSE"
echo

echo -e "${YELLOW}2.4 Testing Librarian Access - Create Book${NC}"
echo "POST $BASE_URL/api/books"
LIBRARIAN_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/books" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Great Gatsby",
    "author": "F. Scott Fitzgerald",
    "isbn": "978-0743273565",
    "barcode": "BOOK001",
    "description": "A story of the fabulously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.",
    "publicationYear": 1925,
    "publisher": "Scribner",
    "genre": "Fiction"
  }')
echo "Response: $LIBRARIAN_CREATE_RESPONSE"
echo

# Extract book ID from response
BOOK_ID=$(echo "$LIBRARIAN_CREATE_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

echo -e "${YELLOW}2.5 Testing Librarian Access - Create Another Book${NC}"
echo "POST $BASE_URL/api/books"
LIBRARIAN_CREATE2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/books" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "To Kill a Mockingbird",
    "author": "Harper Lee",
    "isbn": "978-0446310789",
    "barcode": "BOOK002",
    "description": "The story of young Scout Finch and her father Atticus in a racially divided Alabama town.",
    "publicationYear": 1960,
    "publisher": "Grand Central Publishing",
    "genre": "Fiction"
  }')
echo "Response: $LIBRARIAN_CREATE2_RESPONSE"
echo

echo -e "${YELLOW}2.6 Testing Member Access - View All Books (Now with data)${NC}"
echo "GET $BASE_URL/api/books"
MEMBER_BOOKS2_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_BOOKS2_RESPONSE"
echo

echo -e "${YELLOW}2.7 Testing Member Access - Search by Title${NC}"
echo "GET $BASE_URL/api/books/search?q=Gatsby"
MEMBER_SEARCH2_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/search?q=Gatsby" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_SEARCH2_RESPONSE"
echo

echo -e "${YELLOW}2.8 Testing Member Access - Get Book by ID${NC}"
echo "GET $BASE_URL/api/books/$BOOK_ID"
MEMBER_GET_BOOK_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_GET_BOOK_RESPONSE"
echo

echo -e "${YELLOW}2.9 Testing Member Access - Get Book by Barcode${NC}"
echo "GET $BASE_URL/api/books/barcode/BOOK001"
MEMBER_GET_BARCODE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/barcode/BOOK001" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_GET_BARCODE_RESPONSE"
echo

echo -e "${YELLOW}2.10 Testing Librarian Access - Update Book${NC}"
echo "PUT $BASE_URL/api/books/$BOOK_ID"
LIBRARIAN_UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Great Gatsby (Updated)",
    "author": "F. Scott Fitzgerald",
    "isbn": "978-0743273565",
    "barcode": "BOOK001",
    "description": "Updated description of the fabulously wealthy Jay Gatsby.",
    "publicationYear": 1925,
    "publisher": "Scribner",
    "genre": "Classic Fiction"
  }')
echo "Response: $LIBRARIAN_UPDATE_RESPONSE"
echo

echo -e "${YELLOW}2.11 Testing Librarian Access - Update Book Status${NC}"
echo "PATCH $BASE_URL/api/books/$BOOK_ID/status?status=BORROWED"
LIBRARIAN_STATUS_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/books/$BOOK_ID/status?status=BORROWED" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN")
echo "Response: $LIBRARIAN_STATUS_RESPONSE"
echo

echo -e "${YELLOW}2.12 Testing Member Access - Get Books by Status${NC}"
echo "GET $BASE_URL/api/books/status/BORROWED"
MEMBER_STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/status/BORROWED" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_STATUS_RESPONSE"
echo

echo -e "${BLUE}=== 3. Testing Error Scenarios ===${NC}"
echo

echo -e "${YELLOW}3.1 Testing Duplicate Barcode (Should Fail)${NC}"
echo "POST $BASE_URL/api/books"
DUPLICATE_BARCODE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/books" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Duplicate Book",
    "author": "Test Author",
    "barcode": "BOOK001"
  }')
echo "Response: $DUPLICATE_BARCODE_RESPONSE"
echo

echo -e "${YELLOW}3.2 Testing Non-existent Book (Should Fail)${NC}"
echo "GET $BASE_URL/api/books/99999"
NONEXISTENT_BOOK_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/99999" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $NONEXISTENT_BOOK_RESPONSE"
echo

echo -e "${YELLOW}3.3 Testing Member Access - Delete Book (Should Fail)${NC}"
echo "DELETE $BASE_URL/api/books/$BOOK_ID"
MEMBER_DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_DELETE_RESPONSE"
echo

echo -e "${YELLOW}3.4 Testing Librarian Access - Delete Book${NC}"
echo "DELETE $BASE_URL/api/books/$BOOK_ID"
LIBRARIAN_DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN")
echo "Response: $LIBRARIAN_DELETE_RESPONSE"
echo

echo -e "${YELLOW}3.5 Testing Member Access - Verify Book Deleted${NC}"
echo "GET $BASE_URL/api/books/$BOOK_ID"
MEMBER_VERIFY_DELETE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_VERIFY_DELETE_RESPONSE"
echo

echo -e "${GREEN}=== Book Catalog RBAC Test Complete ===${NC}"
