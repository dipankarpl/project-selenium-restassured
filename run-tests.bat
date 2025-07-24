@echo off
REM Simple test execution script for Windows

echo === Test Automation Framework ===
echo Available commands:
echo 1. sanity    - Run sanity tests
echo 2. regression - Run regression tests
echo 3. api       - Run API tests only
echo 4. ui        - Run UI tests only
echo.

REM Default values
set SUITE=sanity
set BROWSER=chrome
set ENVIRONMENT=qa
set HEADLESS=false

REM Parse command line arguments
:parse_args
if "%1"=="sanity" (
    set SUITE=sanity
    shift
    goto parse_args
)
if "%1"=="regression" (
    set SUITE=regression
    shift
    goto parse_args
)
if "%1"=="api" (
    set SUITE=api
    shift
    goto parse_args
)
if "%1"=="ui" (
    set SUITE=ui
    shift
    goto parse_args
)
if "%1"=="--browser" (
    set BROWSER=%2
    shift
    shift
    goto parse_args
)
if "%1"=="--env" (
    set ENVIRONMENT=%2
    shift
    shift
    goto parse_args
)
if "%1"=="--headless" (
    set HEADLESS=true
    shift
    goto parse_args
)
if "%1"=="--help" (
    echo Usage: run-tests.bat [SUITE] [OPTIONS]
    echo.
    echo SUITE:
    echo   sanity      Run sanity tests (default)
    echo   regression  Run regression tests
    echo   api         Run API tests only
    echo   ui          Run UI tests only
    echo.
    echo OPTIONS:
    echo   --browser BROWSER    Browser to use (chrome, firefox, edge)
    echo   --env ENVIRONMENT    Environment (qa, uat, prod)
    echo   --headless          Run in headless mode
    echo   --help              Show this help
    exit /b 0
)

echo Running tests with:
echo   Suite: %SUITE%
echo   Browser: %BROWSER%
echo   Environment: %ENVIRONMENT%
echo   Headless: %HEADLESS%
echo.

REM Create reports directory
if not exist "reports\extent-report" mkdir "reports\extent-report"
if not exist "reports\screenshots" mkdir "reports\screenshots"
if not exist "logs" mkdir "logs"

REM Run tests based on suite
if "%SUITE%"=="sanity" (
    mvn test -Psanity -Dbrowser=%BROWSER% -Denvironment=%ENVIRONMENT% -Dheadless=%HEADLESS%
) else if "%SUITE%"=="regression" (
    mvn test -Pregression -Dbrowser=%BROWSER% -Denvironment=%ENVIRONMENT% -Dheadless=%HEADLESS%
) else if "%SUITE%"=="api" (
    mvn test -Pregression -Dgroups=api -Denvironment=%ENVIRONMENT%
) else if "%SUITE%"=="ui" (
    mvn test -Pregression -Dgroups=ui -Dbrowser=%BROWSER% -Denvironment=%ENVIRONMENT% -Dheadless=%HEADLESS%
) else (
    echo Unknown suite: %SUITE%
    exit /b 1
)

REM Check if tests passed
if %errorlevel% equ 0 (
    echo.
    echo ✅ Tests completed successfully!
    echo 📊 Reports available in: reports\extent-report\
    echo 📸 Screenshots available in: reports\screenshots\
) else (
    echo.
    echo ❌ Tests failed!
    echo 📊 Check reports in: reports\extent-report\
    echo 📸 Check screenshots in: reports\screenshots\
    echo 📝 Check logs in: logs\
)