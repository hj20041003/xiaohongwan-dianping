@echo off
rem HM-dianping MICROSERVICE edition one-click start
rem Order: Nacos(8848) -> user/shop/voucher/blog -> gateway(8090). nginx(8080) starts at the end if not running.
echo [1/4] Starting Nacos (wait ~40s)...
start "nacos" /min cmd /c "D:\dianping-app\cloud-scripts\nacos-start.cmd"
timeout /t 40 /nobreak >nul
echo [2/4] Starting services: user(8082) shop(8083) voucher(8084) blog(8085)...
start "hmdp-user" /min cmd /c "D:\dianping-app\cloud-scripts\svc-user.cmd"
start "hmdp-shop" /min cmd /c "D:\dianping-app\cloud-scripts\svc-shop.cmd"
start "hmdp-voucher" /min cmd /c "D:\dianping-app\cloud-scripts\svc-voucher.cmd"
start "hmdp-blog" /min cmd /c "D:\dianping-app\cloud-scripts\svc-blog.cmd"
echo Waiting ~50s for services...
timeout /t 50 /nobreak >nul
echo [3/4] Starting gateway (8090)...
start "hmdp-gateway" /min cmd /c "D:\dianping-app\cloud-scripts\svc-gateway.cmd"
echo Waiting ~25s for gateway...
timeout /t 25 /nobreak >nul
echo [4/4] Starting nginx (8080), skipped if already running...
cd /d "D:\dianping-app\HM-dianping\hmdp\nginx-1.18.0"
if not exist logs mkdir logs
if not exist temp mkdir temp
tasklist | findstr /i nginx.exe >nul
if errorlevel 1 start "" nginx.exe
echo.
echo ==============================================
echo  Frontend : http://localhost:8080
echo  Score    : http://localhost:8080/score.html
echo  Favorite : http://localhost:8080/favorite.html
echo  Nacos    : http://localhost:8848/nacos (nacos/nacos)
echo  Gateway  : http://localhost:8090
echo ==============================================
