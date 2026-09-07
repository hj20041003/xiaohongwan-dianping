@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-11"
set "JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8"
set "SERVER_PORT=8082"
cd /d "D:\dianping-app\hmdp-cloud\hmdp-user"
call "D:\idea-pro\IntelliJ IDEA 2023.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -DskipTests "-Dspring-boot.run.arguments=--server.port=8082"
