#!/bin/bash
#该脚本用于启动server时做一些初始化工作
# shellcheck disable=SC2164
#外部动态获取并传入
reportDir=$1
echo "reportDir=$reportDir"
echo "s1=$1"
echo "s2=$2"
echo "s3=$3"
#--ssl 开启https会导致：Error: Could not find certificate cert.pem

http-server "$reportDir" -p 8080 --gzip c10
