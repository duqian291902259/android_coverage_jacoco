#!/bin/bash
#note by dq:该脚本用于node-http-server自动打包构建，部署
cd ../
cd web-vue && npm run build
cd ../
#cp -p -r target/classes/web/*  http-server/
cp -p -r src/main/resources/web/*  http-server/

#docker 构建并node-http-server服务
# shellcheck disable=SC2164
cd http-server/
docker build -t http-server .
docker tag http-server:latest http-server:1.0.0
docker push duqian2010/http-server:1.0.0
#docker login ncr-dev.duqian.cn
#docker push ncr-dev.duqian.cn/duqian02/android/http-server:latest

# publish
#curl https://hub.docker.com/repository/docker/duqian2010/android/webhooks/1

#docker run http-server:1.0.0
#docker run --net-host http-server -i -t http-server:latest   /bin/bash
docker run --network host duqian2010/http-server:1.0.0

docker login
docker push duqian2010/http-server:1.0.0
#The push refers to repository [docker.io/duqian2010/http-server]
