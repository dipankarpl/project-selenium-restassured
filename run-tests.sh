#!/bin/bash

# Simple test execution script

echo "=== Test Automation Framework ==="
echo "Available commands:"
echo "1. sanity    - Run sanity tests"
echo "2. regression - Run regression tests"
echo "3. api       - Run API tests only"
echo "4. ui        - Run UI tests only"
echo ""

# Default values
SUITE="sanity"
BROWSER="chrome"
ENVIRONMENT="qa"
HEADLESS="false"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        sanity|regression|api|ui)
            SUITE="$1"
            shift
            ;;
        --browser)
            BROWSER="$2"
            shift 2
            ;;
        --env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --headless)
            HEADLESS="true"
            shift
            ;;
        --help)
            echo "Usage: ./run-tests.sh [SUITE] [OPTIONS]"
            echo ""
            echo "SUITE:"
            echo "  sanity      Run sanity tests (default)"
            echo "  regression  Run regression tests"
            echo "  api         Run API tests only"
            echo "  ui          Run UI tests only"
            echo ""
            echo "OPTIONS:"
            echo "  --browser BROWSER    Browser to use (chrome, firefox, edge)"
            echo "  --env ENVIRONMENT    Environment (qa, uat, prod)"
            echo "  --headless          Run in headless mode"
            echo "  --help              Show this help"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo "Running tests with:"
echo "  Suite: $SUITE"
echo "  Browser: $BROWSER"
echo "  Environment: $ENVIRONMENT"
echo "  Headless: $HEADLESS"
echo ""

# Create reports directory
mkdir -p reports/extent-report
mkdir -p reports/screenshots
mkdir -p logs

# Run tests based on suite
case $SUITE in
    sanity)
        mvn test -Psanity -Dbrowser=$BROWSER -Denvironment=$ENVIRONMENT -Dheadless=$HEADLESS
        ;;
    regression)
        mvn test -Pregression -Dbrowser=$BROWSER -Denvironment=$ENVIRONMENT -Dheadless=$HEADLESS
        ;;
    api)
        mvn test -Pregression -Dgroups=api -Denvironment=$ENVIRONMENT
        ;;
    ui)
        mvn test -Pregression -Dgroups=ui -Dbrowser=$BROWSER -Denvironment=$ENVIRONMENT -Dheadless=$HEADLESS
        ;;
    *)
        echo "Unknown suite: $SUITE"
        exit 1
        ;;
esac

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Tests completed successfully!"
    echo "📊 Reports available in: reports/extent-report/"
    echo "📸 Screenshots available in: reports/screenshots/"
else
    echo ""
    echo "❌ Tests failed!"
    echo "📊 Check reports in: reports/extent-report/"
    echo "📸 Check screenshots in: reports/screenshots/"
    echo "📝 Check logs in: logs/"
fi