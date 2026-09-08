@echo off
setlocal
set BASE_DIR=%~dp0
set DIST=%BASE_DIR%.gradle-dist\gradle-8.14.3\bin\gradle.bat
if exist "%DIST%" goto run
if not exist "%BASE_DIR%.gradle-dist" mkdir "%BASE_DIR%.gradle-dist"
set ZIP=%BASE_DIR%.gradle-dist\gradle-8.14.3-bin.zip
where curl.exe >nul 2>nul
if %errorlevel%==0 (curl.exe -L --fail --retry 3 -o "%ZIP%" https://services.gradle.org/distributions/gradle-8.14.3-bin.zip) else (powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.14.3-bin.zip' -OutFile '%ZIP%'")
powershell -NoProfile -Command "Expand-Archive -Force '%ZIP%' '%BASE_DIR%.gradle-dist'"
:run
call "%DIST%" %*
