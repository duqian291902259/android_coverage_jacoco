#!/bin/bash
#note by dq:该脚本用于Android镜像打包构建
docker build -t android .
docker tag android:latest android:1.0.0
docker push duqian2010/android:1.0.0
#docker login ncr-dev.duqian.cn
#docker push ncr-dev.duqian.cn/duqian02/android/android:latest

# publish
#curl https://hub.docker.com/repository/docker/duqian2010/android/webhooks/1

docker run android:1.0.0

docker login
docker push duqian2010/android:1.0.0
#The push refers to repository [docker.io/duqian2010/android]
