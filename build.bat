@echo off
setlocal

REM GymLog build script (JDK 17 + Android SDK + Gradle 8.7)
set "JDK_DIR=C:\Users\Symbol\android\jdk"
set "SDK_DIR=C:\Users\Symbol\android\sdk"
set "GRADLE_DIR=C:\Users\Symbol\android\gradle-8.7"

if not defined JAVA_HOME (
    for /d %%D in ("%JDK_DIR%\jdk-*") do set "JAVA_HOME=%%D"
)
if not defined JAVA_HOME (
    echo [ERROR] JDK 17 not found
    exit /b 1
)
if not defined ANDROID_HOME set "ANDROID_HOME=%SDK_DIR%"

set "GRADLE=%GRADLE_DIR%\bin\gradle.bat"
if not exist "%GRADLE%" (
    echo [ERROR] gradle not found
    exit /b 1
)

echo JAVA_HOME=%JAVA_HOME%
echo ANDROID_HOME=%ANDROID_HOME%
echo Building debug APK ...
call "%GRADLE%" assembleDebug
if errorlevel 1 (
    echo [ERROR] build failed
    exit /b 1
)
echo.
echo BUILD OK: app\build\outputs\apk\debug\app-debug.apk
endlocal
