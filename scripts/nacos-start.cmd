@echo off
set "SERVER_PORT=8848"
"C:\Program Files\Java\jdk-11\bin\java.exe" -Xms512m -Xmx512m -Xmn256m -Dnacos.standalone=true -Dnacos.home="D:\dianping-app\nacos2" -jar "D:\dianping-app\nacos2\target\nacos-server.jar" --spring.config.additional-location=file:"D:\dianping-app\nacos2\conf/" --logging.config="D:\dianping-app\nacos2\conf\nacos-logback.xml" nacos.nacos
