#基础镜像
#FROM java:8
#FROM hub.c.163.com/library/java:latest
FROM java:8-jdk-alpine
VOLUME /cc-jacoco-download
ADD target/jacoco.web-1.0.0.jar jacoco-web.jar
ADD jacococli.jar jacococli.jar
RUN chmod 775 jacococli.jar
RUN chmod +x jacococli.jar

# install git - apt-get replace with apk
RUN apk update && \
    apk upgrade && \
    apk add --no-cache bash git nodejs

RUN npm install http-server -g

# 运行jar包
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/jacoco-web.jar"]

#维护者
MAINTAINER duqian2010@gmail.com

#CMD ["http-server","cc-jacoco-download/","-p","8086"]
#CMD ["cd","/cc-jacoco-download/"]
#CMD ["http-server"]
# docker build -t jacoco-web .
# docker run -d -p 8090:8090 -p 8086:8086 jacoco-web:latest jacoco-web:latest