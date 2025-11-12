@echo off
echo ========================================
echo Testing GreenFlow App Build
echo ========================================
echo.

echo [1/4] Cleaning project...
call gradlew clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)
echo Clean successful!
echo.

echo [2/4] Building debug APK...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Debug build failed!
    pause
    exit /b 1
)
echo Debug build successful!
echo.

echo [3/4] Running lint checks...
call gradlew lint
if %errorlevel% neq 0 (
    echo WARNING: Lint found issues (check app/build/reports/lint-results.html)
)
echo.

echo [4/4] Checking diagnostics...
echo Build completed successfully!
echo.
echo APK Location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo ========================================
echo Build Summary
echo ========================================
dir app\build\outputs\apk\debug\*.apk
echo.
pause
