@echo off
chcp 65001 >nul
echo ========================================================
echo   Starting Morrow Online Learning Platform...
echo ========================================================

echo [1/2] Starting Backend (Spring Boot on port 8081)...
start "Backend - Spring Boot" cmd /k "cd /d "%~dp0backend" && mvnw.cmd spring-boot:run"

echo Waiting 5 seconds for backend initialization...
timeout /t 5 /nobreak >nul

echo [2/2] Starting Frontend (Next.js on port 3000)...
start "Frontend - Next.js" cmd /k "cd /d "%~dp0frontend" && npm run dev"

echo.
echo Opening browser at http://localhost:3000 ...
timeout /t 3 /nobreak >nul
start http://localhost:3000

echo.
echo ========================================================
echo   Dự án đã được khởi chạy thành công!
echo   - Frontend: http://localhost:3000
echo   - Backend API: http://localhost:8081
echo ========================================================
