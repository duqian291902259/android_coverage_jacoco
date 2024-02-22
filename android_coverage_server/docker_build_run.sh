#!/bin/bash
#docker 构建并node-http-server服务
# shellcheck disable=SC2164
TAG="latest"

#docker 构建jacoco-vue前端服务
cd http-server/
ls
#sh build_node.sh
docker build -t http-server .
docker tag http-server:latest duqian2010/http-server:$TAG
cd ../
ls
#docker 构建jacoco-server后端服务
cp -p target/jacoco.web-1.0.0.jar jar/
#TAG="3.0.1"

cd docker/
ls
docker build -t jacoco-server .
docker tag jacoco-server:latest duqian2010/jacoco-server:$TAG

#发布镜像到本地测试
docker-compose up -d

