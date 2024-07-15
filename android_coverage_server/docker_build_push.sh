#!/bin/bash
#docker 构建并node-http-server服务
# shellcheck disable=SC2164
TAG="latest"

cd http-server/
docker build -t http-server .
docker tag http-server:latest duqian2010/http-server:$TAG
docker login
docker push duqian2010/http-server:$TAG

#docker 构建jacoco-web服务
cd ../
cp -p target/jacoco.web-1.0.0.jar jar/
#TAG="3.0.1"
cd jacoco-server/
docker build -t jacoco-server .
docker tag jacoco-server:latest duqian2010/jacoco-server:$TAG

#login dockerhub and push images
docker login
docker push duqian2010/jacoco-server:$TAG
docker push duqian2010/http-server:$TAG

#发布镜像到本地测试
docker-compose up -d

