@echo off
rem Stop the microservice stack (nginx, gateway, 4 services, nacos)
taskkill /F /IM nginx.exe 2>nul
for %%p in (8090 8082 8083 8084 8085 8848) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p " ^| findstr "LISTENING"') do (
        taskkill /F /PID %%a >nul 2>&1
    )
)
echo Microservice stack stopped.
