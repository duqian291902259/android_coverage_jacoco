#基础镜像
#FROM java:8
#FROM hub.c.163.com/library/java:latest
FROM java:8-jdk-alpine
VOLUME /cc-jacoco-download
ADD jacoco.web-1.0.0.jar jacoco-web.jar

# install git - apt-get replace with apk
RUN apk update && \
    apk upgrade && \
    apk add --no-cache bash git node


ADD jacococli.jar jacococli.jar
RUN chmod 775 jacococli.jar
# 运行jar包
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/jacoco-web.jar"]

#维护者
MAINTAINER duqian2010@gmail.com