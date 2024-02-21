#基础镜像 #AS jacoco_image
#FROM java:8-jdk-alpine
FROM alpine:3.14
VOLUME /dq-coverage
# 工作目录
#WORKDIR /duqian02
RUN echo "Asia/Shanghai" > /etc/timezone
# 不起作用 RUN rm -rf
ADD jacoco.web-1.0.0.jar jacoco-server.jar
ADD jacococli.jar jacococli.jar
ADD cmdShell.sh cmdShell.sh
#ADD dq-coverage dq-coverage
#ADD dq-coverage/cc-ios/ dq-coverage/cc-ios/
#ADD dq-coverage/cc-pc/ dq-coverage/cc-pc/
#COPY dq-coverage /
RUN chmod 775 jacococli.jar
RUN chmod +x cmdShell.sh
RUN chmod +x jacococli.jar
RUN chmod +x jacoco-server.jar

#RUN chmod +x dq-coverage

# --no-cache nodejs
RUN apk update && \
    apk upgrade && \
    apk add bash git p7zip curl unzip

RUN apk add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
RUN export JAVA_HOME=/opt/openjdk-11
RUN export PATH=$JAVA_HOME/bin:$PATH


EXPOSE 8090

# 运行jar包
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","jacoco-server.jar"]

#维护者
MAINTAINER 杜小菜,duqian2010@gmail.com

#docker build -t jacoco-server .
#docker run --rm -p 18080:8090 jacoco-server:latest

# 先package！！在构建docker容器，push镜像

# docker stop xx
# duqian02/Dusa0..
# docker login site.duqian.cn

# docker build -t jacoco-server .
# docker run --rm -p 18090:8090 -p 18080:8080 -p 18081:8081 jacoco-server:latest

# docker tag jacoco-server:latest site.duqian.cn/duqian02/android/jacoco-server:latest
# docker push site.duqian.cn/duqian02/android/jacoco-server:latest
# docker run --rm -p 18090:8090 -p 18080:8080 site.duqian.cn/duqian02/android/jacoco-server:latest

# 手动更新镜像-》android @  http://portainer.duqian.cn/#!/1/docker/stacks/jacoco?id=53&type=1&regular=true&external=false&orphaned=false

# 常见报错：
# docker未启动，ERROR: Error response from daemon: open \.\pipe\docker_engine_linux: The system cannot find the file specified.errors pretty printing info
#Net stop com.docker.service
#Net start com.docker.service