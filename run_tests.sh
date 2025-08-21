#!/bin/bash

# Huspark Library Management System - Test Runner
# This script runs all test cases for the project

echo "=========================================="
echo "Huspark Library Management System"
echo "Comprehensive Test Suite Runner"
echo "=========================================="

# Set colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

print_status "Maven version: $(mvn --version | head -n 1)"

# Clean and compile the project
print_status "Cleaning and compiling the project..."
if mvn clean compile -q; then
    print_success "Project compiled successfully"
else
    print_error "Project compilation failed"
    exit 1
fi

# Run all tests
print_status "Running all test cases..."
echo ""

# Run tests with detailed output
if mvn test -Dspring.profiles.active=test; then
    print_success "All tests passed successfully!"
else
    print_error "Some tests failed. Please check the output above."
    exit 1
fi

echo ""
print_status "Test Summary:"
echo "=========================================="

# Run specific test categories
print_status "Running Repository Tests..."
mvn test -Dtest="*RepositoryTest" -q

print_status "Running Service Tests..."
mvn test -Dtest="*ServiceTest" -q

print_status "Running Controller Tests..."
mvn test -Dtest="*ControllerTest" -q

print_status "Running Integration Tests..."
mvn test -Dtest="*IntegrationTest" -q

echo ""
print_status "Generating test report..."
mvn surefire-report:report -q

if [ -f "target/site/surefire-report.html" ]; then
    print_success "Test report generated: target/site/surefire-report.html"
else
    print_warning "Test report generation failed"
fi

echo ""
print_status "Test Coverage Analysis..."
if mvn jacoco:report -q; then
    if [ -f "target/site/jacoco/index.html" ]; then
        print_success "Coverage report generated: target/site/jacoco/index.html"
    else
        print_warning "Coverage report generation failed"
    fi
else
    print_warning "Coverage analysis failed - Jacoco plugin may not be configured"
fi

echo ""
echo "=========================================="
print_success "Test execution completed!"
echo "=========================================="

# Display test statistics
echo ""
print_status "Test Statistics:"
echo "------------------------------------------"

# Count test classes
REPO_TESTS=$(find src/test/java -name "*RepositoryTest.java" | wc -l)
SERVICE_TESTS=$(find src/test/java -name "*ServiceTest.java" | wc -l)
CONTROLLER_TESTS=$(find src/test/java -name "*ControllerTest.java" | wc -l)
INTEGRATION_TESTS=$(find src/test/java -name "*IntegrationTest.java" | wc -l)

echo "Repository Tests: $REPO_TESTS"
echo "Service Tests: $SERVICE_TESTS"
echo "Controller Tests: $CONTROLLER_TESTS"
echo "Integration Tests: $INTEGRATION_TESTS"

TOTAL_TESTS=$((REPO_TESTS + SERVICE_TESTS + CONTROLLER_TESTS + INTEGRATION_TESTS))
echo "Total Test Classes: $TOTAL_TESTS"

echo ""
print_status "Available Test Reports:"
echo "------------------------------------------"
echo "• Surefire Report: target/site/surefire-report.html"
echo "• Coverage Report: target/site/jacoco/index.html"
echo "• Test Results: target/surefire-reports/"

echo ""
print_status "Next Steps:"
echo "------------------------------------------"
echo "1. Review test results in the generated reports"
echo "2. Check test coverage to identify uncovered code"
echo "3. Add more tests for any missing scenarios"
echo "4. Run specific test categories as needed:"
echo "   - Repository: mvn test -Dtest=\"*RepositoryTest\""
echo "   - Service: mvn test -Dtest=\"*ServiceTest\""
echo "   - Controller: mvn test -Dtest=\"*ControllerTest\""

echo ""
print_success "Test suite execution completed successfully!"
