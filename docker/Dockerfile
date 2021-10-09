#基础镜像
#FROM java:8
#FROM hub.c.163.com/library/java:latest
FROM java:8-jdk-alpine
VOLUME /jacoco_tmp
#ADD jacoco.web-1.0.0.jar jacoco-web.jar
ADD cc-jacoco-helper.jar jacoco-web.jar
# 运行jar包
RUN bash -c 'touch /jacoco-web.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/jacoco-web.jar"]

#维护者
MAINTAINER duqian2010@gmail.com