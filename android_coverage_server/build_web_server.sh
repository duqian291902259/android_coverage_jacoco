#!/bin/bash
#note by dq:该脚本用于自动打包构建，部署server
cd web-vue && npm run build
cd ../
mvn clean kotlin:compile package -Dmaven.test.skip=true
cp -p target/jacoco.web-1.0.0.jar  jacoco-server/
cp -p -r src/main/resources/web/*  http-server/
#重命名
#mv http-server/index.html http-server/index2.html
