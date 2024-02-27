#!/bin/bash
#note by dq:该脚本用于自动打包构建jacoco-web，部署server,
# 并推送到site.duqian.cn/duqian02/android/jacoco-web，
# 目前项目是非公开的，使用Portainer如果更新不到镜像，就在项目配置管理里面手动勾选“公开”，再拉取镜像。
cd ../
mvn clean kotlin:compile package -Dmaven.test.skip=true
cp -p target/jacoco.web-1.0.0.jar  jacoco-server/

#docker 构建并jacoco-server
# shellcheck disable=SC2164
#TAG="latest"
TAG="1.1.0"
cd jacoco-server
docker build -t jacoco-server .
docker tag jacoco-server:latest duqian2010/jacoco-server:$TAG
docker login
docker push duqian2010/jacoco-server:$TAG

# publish
#curl --location --request POST http://portainer.duqian.cn/api/webhooks/aa0e1265-8dad-4da8-b1e1-081da7497983?tag=$TAG




