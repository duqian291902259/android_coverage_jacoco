#!/bin/bash
#note by dq:该脚本用于自动打包构建，部署server
cd web-vue && npm run build
cd ../
mvn clean kotlin:compile package -Dmaven.test.skip=true
cp -p target/jacoco.web-1.0.0.jar  docker/
#cp -p target/jacoco.web-1.0.0.jar  jar/
cp -p -r src/main/resources/web/*  http-server/
#重命名
#mv http-server/index.html http-server/index2.html

#docker 构建并jacoco-web服务
# shellcheck disable=SC2164
docker build -t jacoco-web .
#docker tag jacoco-web:latest site.duqian.www/android/jacoco-web:latest
#docker login site.duqian.www
#docker push site.duqian.www/android/jacoco-web:latest

#docker 构建并node-http-server服务
# shellcheck disable=SC2164
cd http-server/
docker build -t http-server .
#docker tag http-server:latest site.duqian.www/android/http-server:latest
#docker login site.duqian.www
#docker push site.duqian.www/android/http-server:latest

# publish
#curl http://portainer.duqian.cn/api/webhooks/7eaeab63-6d69-4667-91bf-3b80fc40e0b3

#发布镜像到本地测试
docker-compose up -d
