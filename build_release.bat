@echo off
echo ========================================
echo Building GreenFlow Release APK
echo ========================================
echo.

echo WARNING: This will create an optimized release build
echo with ProGuard enabled. Make sure to test thoroughly!
echo.
pause

echo [1/3] Cleaning project...
call gradlew clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)
echo.

echo [2/3] Building release APK...
call gradlew assembleRelease
if %errorlevel% neq 0 (
    echo ERROR: Release build failed!
    echo Check ProGuard rules if build fails
    pause
    exit /b 1
)
echo.

echo [3/3] Build completed!
echo.
echo ========================================
echo Release APK Location:
echo ========================================
dir app\build\outputs\apk\release\*.apk
echo.
echo NOTE: This APK needs to be signed before publishing
echo Use Android Studio or jarsigner to sign the APK
echo.
pause
