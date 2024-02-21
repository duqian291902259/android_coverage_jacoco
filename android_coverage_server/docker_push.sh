#!/bin/bash
#docker 构建并node-http-server服务
# shellcheck disable=SC2164
cd http-server/
docker build -t http-server .
docker tag http-server:latest site.duqian.www/android/http-server:latest
#docker login site.duqian.www
#docker push site.duqian.www/android/http-server:latest

#docker 构建jacoco-web服务
cp -p target/jacoco.web-1.0.0.jar jar/
docker build -t jacoco-web .
docker tag jacoco-web:latest site.duqian.www/android/jacoco-web:latest
#docker login site.duqian.www
#docker push site.duqian.www/android/jacoco-web:latest
