@echo off
title Babysitter Booking - Launcher
echo ===============================================
echo  Babysitter Booking Platform - starting...
echo ===============================================
echo.

echo [0/2] Checking PostgreSQL (Docker)...
docker ps | findstr babysitter-postgres >nul
if %errorlevel% neq 0 (
  echo   PostgreSQL not running, starting it...
  docker start babysitter-postgres >nul 2>&1
  if %errorlevel% neq 0 (
    echo   No existing container, creating new one...
    docker run -d --name babysitter-postgres -e POSTGRES_DB=babysitter_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:17-alpine >nul
  )
  echo   Waiting for PostgreSQL to be ready...
  timeout /t 5 /nobreak >nul
) else (
  echo   PostgreSQL is already running.
)

echo [1/2] Starting backend (Spring Boot) in a new window...
start "Babysitter Backend" cmd /k "cd /d D:\anascapstone\backend && mvn spring-boot:run"

echo [2/2] Starting frontend (Vite + React) in a new window...
start "Babysitter Frontend" cmd /k "cd /d D:\anascapstone\frontend && npm run dev"

echo.
echo Done! Two new windows were opened:
echo   - PostgreSQL        : localhost:5432 (db: babysitter_db, user: postgres / postgres)
echo   - Babysitter Backend  : http://localhost:8080  (Swagger UI: /swagger-ui.html, Health: /api/health)
echo   - Babysitter Frontend : http://localhost:5173
echo.
echo Keep all windows open while you use the app.
echo To stop a server, press Ctrl+C inside its window (or just close it).
pause
