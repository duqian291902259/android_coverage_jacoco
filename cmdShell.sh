#!/bin/bash
#该脚本用于启动server时做一些初始化工作

#外部动态获取并传入
reportDir=$2
echo "reportDir=$reportDir"
http-server $reportDir -p 8087
#http-server / -p 8085