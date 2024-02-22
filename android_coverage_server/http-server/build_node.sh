#!/bin/bash
#note by dq:该脚本用于node-http-server自动打包构建，部署
cd ../
cd web-vue && npm run build
cd ../
#cp -p -r target/classes/web/*  http-server/
cp -p -r src/main/resources/web/*  http-server/

#docker 构建并node-http-server服务
# shellcheck disable=SC2164
TAG="latest"
cd http-server/
docker build -t http-server .
docker tag http-server:latest duqian2010/http-server:$TAG
docker login
docker push duqian2010/http-server:$TAG

# publish
#curl https://hub.docker.com/repository/docker/duqian2010/android/webhooks/1
#docker run http-server:last

