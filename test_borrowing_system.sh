#!/bin/bash

echo "=== Testing HuSpark Borrowing System ==="
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

# Create test users
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

echo -e "${BLUE}=== 2. Testing Book Availability ===${NC}"
echo

echo -e "${YELLOW}2.1 View Available Books${NC}"
echo "GET $BASE_URL/api/books"
AVAILABLE_BOOKS=$(curl -s -X GET "$BASE_URL/api/books" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $AVAILABLE_BOOKS"
echo

# Extract first book ID for testing
BOOK_ID=$(echo "$AVAILABLE_BOOKS" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)
echo -e "${GREEN}Using Book ID: $BOOK_ID for testing${NC}"
echo

echo -e "${BLUE}=== 3. Testing Borrowing Operations ===${NC}"
echo

echo -e "${YELLOW}3.1 Borrow a Book${NC}"
echo "POST $BASE_URL/api/borrowing/borrow"
BORROW_RESPONSE=$(curl -s -X POST "$BASE_URL/api/borrowing/borrow" \
  -H "Authorization: Bearer $MEMBER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"bookId\": $BOOK_ID
  }")
echo "Response: $BORROW_RESPONSE"
echo

# Extract transaction ID
TRANSACTION_ID=$(echo "$BORROW_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo -e "${GREEN}Transaction ID: $TRANSACTION_ID${NC}"
echo

echo -e "${YELLOW}3.2 Check Current Borrowings${NC}"
echo "GET $BASE_URL/api/borrowing/current"
CURRENT_BORROWINGS=$(curl -s -X GET "$BASE_URL/api/borrowing/current" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $CURRENT_BORROWINGS"
echo

echo -e "${YELLOW}3.3 Try to Borrow Same Book Again (Should Fail)${NC}"
echo "POST $BASE_URL/api/borrowing/borrow"
DUPLICATE_BORROW_RESPONSE=$(curl -s -X POST "$BASE_URL/api/borrowing/borrow" \
  -H "Authorization: Bearer $MEMBER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"bookId\": $BOOK_ID
  }")
echo "Response: $DUPLICATE_BORROW_RESPONSE"
echo

echo -e "${YELLOW}3.4 Check Book Status After Borrowing${NC}"
echo "GET $BASE_URL/api/books/$BOOK_ID"
BOOK_STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $BOOK_STATUS_RESPONSE"
echo

echo -e "${BLUE}=== 4. Testing Returning Operations ===${NC}"
echo

echo -e "${YELLOW}4.1 Return the Book${NC}"
echo "POST $BASE_URL/api/borrowing/return/$BOOK_ID"
RETURN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/borrowing/return/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $RETURN_RESPONSE"
echo

echo -e "${YELLOW}4.2 Check Book Status After Returning${NC}"
echo "GET $BASE_URL/api/books/$BOOK_ID"
BOOK_STATUS_AFTER_RETURN=$(curl -s -X GET "$BASE_URL/api/books/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $BOOK_STATUS_AFTER_RETURN"
echo

echo -e "${YELLOW}4.3 Check Borrowing History${NC}"
echo "GET $BASE_URL/api/borrowing/history"
BORROWING_HISTORY=$(curl -s -X GET "$BASE_URL/api/borrowing/history" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $BORROWING_HISTORY"
echo

echo -e "${BLUE}=== 5. Testing Librarian Functions ===${NC}"
echo

echo -e "${YELLOW}5.1 Librarian View Overdue Transactions${NC}"
echo "GET $BASE_URL/api/borrowing/overdue"
OVERDUE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/borrowing/overdue" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN")
echo "Response: $OVERDUE_RESPONSE"
echo

echo -e "${YELLOW}5.2 Librarian View Reminder Transactions${NC}"
echo "GET $BASE_URL/api/borrowing/reminders"
REMINDERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/borrowing/reminders" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN")
echo "Response: $REMINDERS_RESPONSE"
echo

echo -e "${YELLOW}5.3 Librarian Calculate Late Fees${NC}"
echo "POST $BASE_URL/api/borrowing/late-fees/calculate"
LATE_FEES_RESPONSE=$(curl -s -X POST "$BASE_URL/api/borrowing/late-fees/calculate" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN")
echo "Response: $LATE_FEES_RESPONSE"
echo

echo -e "${YELLOW}5.4 Librarian View Inactive Users${NC}"
echo "GET $BASE_URL/api/borrowing/inactive-users"
INACTIVE_USERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/borrowing/inactive-users" \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN")
echo "Response: $INACTIVE_USERS_RESPONSE"
echo

echo -e "${BLUE}=== 6. Testing Error Scenarios ===${NC}"
echo

echo -e "${YELLOW}6.1 Try to Borrow Non-existent Book${NC}"
echo "POST $BASE_URL/api/borrowing/borrow"
NONEXISTENT_BOOK_RESPONSE=$(curl -s -X POST "$BASE_URL/api/borrowing/borrow" \
  -H "Authorization: Bearer $MEMBER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 99999
  }')
echo "Response: $NONEXISTENT_BOOK_RESPONSE"
echo

echo -e "${YELLOW}6.2 Try to Return Book Not Borrowed${NC}"
echo "POST $BASE_URL/api/borrowing/return/$BOOK_ID"
NOT_BORROWED_RESPONSE=$(curl -s -X POST "$BASE_URL/api/borrowing/return/$BOOK_ID" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $NOT_BORROWED_RESPONSE"
echo

echo -e "${YELLOW}6.3 Member Try to Access Librarian Function${NC}"
echo "GET $BASE_URL/api/borrowing/overdue"
MEMBER_ACCESS_DENIED=$(curl -s -X GET "$BASE_URL/api/borrowing/overdue" \
  -H "Authorization: Bearer $MEMBER_TOKEN")
echo "Response: $MEMBER_ACCESS_DENIED"
echo

echo -e "${GREEN}=== Borrowing System Test Complete ===${NC}"
