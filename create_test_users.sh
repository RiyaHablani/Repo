#!/bin/bash

# Script to create test users for advanced features testing

echo "Creating test users..."

# Create admin user
echo "Creating admin user..."
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@library.com",
    "password": "admin123"
  }' | jq .

echo ""

# Create librarian user
echo "Creating librarian user..."
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Librarian User",
    "email": "librarian@library.com",
    "password": "librarian123"
  }' | jq .

echo ""

# Create member user
echo "Creating member user..."
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Member User",
    "email": "member@library.com",
    "password": "member123"
  }' | jq .

echo ""
echo "Test users created successfully!"
