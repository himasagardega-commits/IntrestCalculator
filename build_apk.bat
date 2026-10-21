@echo off
echo ========================================================
echo  Building Interest Calculator Android Application (APK)
echo ========================================================

set JAVA_HOME=d:\tools\jdk-17.0.10+7
set ANDROID_HOME=d:\tools\android-sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%PATH%

echo Java Home: %JAVA_HOME%
echo Android Home: %ANDROID_HOME%
echo.

call gradlew.bat assembleRelease
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed! Check log messages above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] APK generated successfully at:
echo app\build\outputs\apk\release\app-release.apk
echo.
pause
