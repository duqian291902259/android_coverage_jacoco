### Jacoco-Web-Server
Code-coverage platform base on Jacoco.
Web & Server for Coverage. --> [http://jacoco.dev.cn/](http://jacoco.dev.cn/)  -->by DQ

coverage，[覆盖率报告平台](http://report.dev.cn/)

### web-vue 前端工程

### src:SpringBoot Server

### docker：脚本自动生成并push jacoco服务镜像

### http-server：sh脚本构建http-server服务用于静态报告在线预览

### Dockerfile：定义镜像

###  docker build、push、run
如：运行项目
```
$ docker run jacoco-server:latest -p 8090:8090 --add-host="host.docker.internal:host-gateway"
```

### docker-compose.yml
定义组合镜像，build、push、run

### shell文件：定义构建命令集

### log：保存服务器生成的日志

### coverage：保存端上或者开发机上传的class，src，ec文件，以及生成的报告目录

### cache:gitlab缓存目录（是否有效？）

### target:构建目录，本地忽略，package后的产物

### 平台使用说明

coverage-Web-1.0.0

1，下载安装APK,确认apk构建时对应的提交点。

2，运行APP：安装Debug的包，打开APP运行、测试，会生成待上传覆盖率文件，.ec文件在sdcard的/Android/data/com.netease.cc/cache/coverage/目录下。

3，上传覆盖率文件：进入我的-》设置-》关于-》前往调试控制面板-》生成并上传覆盖率文件；也可以主动清除所有覆盖率文件。

4，生成报告：打开本平台，选择步骤1中apk对应的分支名生成报告，系统会自动获取所选分支最新上传ec文件时的commitId

5，全量&增量报告：勾选增量，需要输入待对比的CommitId或者分支，生成增量报告；如果选择对比分支，commit2无需填写，目前基准分支只提供dev，master（增量差异可能较大）。

6，报告查看：生成增量报告后，可以在线预览，也可以保存到本地。

7，报告管理：覆盖率报告管理页面，可以看到目前已经生成过的报告。

8，上传和下载功能，是用于手动上传class,src,ec等文件，也可以gradle脚本一件操作。

9，其他相关功能正在不断迭代...


### 项目不断完善中...

欢迎提供建议或意见，平台将不断完善。有任何问题或者建议请联系 杜小菜