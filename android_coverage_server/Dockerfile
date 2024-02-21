#基础镜像 #AS jacoco_image
#FROM node:14.15.4 AS node_image
#MAINTAINER 杜小菜,duqian2010@gmail.com
#VOLUME /cc_jacoco
#WORKDIR /node
#RUN echo "NODE Version:" && node --version
#RUN echo "NPM Version:" && npm --version
#RUN npm install http-server -g
#RUN npm install http-server
#CMD ["http-server","dq-coverage","-p","8080"]


FROM java:8-jdk-alpine
#COPY --from=node_image ./node .
VOLUME /dq-coverage
RUN echo "Asia/Shanghai" > /etc/timezone

# 不起作用 RUN rm -rf

ADD target/jacoco.web-1.0.0.jar jacoco-web.jar
#ADD jar/jacoco.web-1.0.0.jar jacoco-web.jar
ADD jacococli.jar jacococli.jar
ADD cmdShell.sh cmdShell.sh
#ADD ./dq-coverage ./dq-coverage
#COPY dq-coverage /
#ADD . .
RUN chmod 775 jacococli.jar
RUN chmod +x cmdShell.sh
RUN chmod +x jacoco-web.jar
RUN chmod +x jacococli.jar
#RUN chmod +x dq-coverage

# --no-cache nodejs
RUN apk update && \
    apk upgrade && \
    apk add bash git p7zip

#RUN npm install http-server -g

#EXPOSE 8080 8090
#CMD ["http-server","dq-coverage","-p","8080"]
# 运行jar包
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/jacoco-web.jar"]

#维护者
MAINTAINER 杜小菜,duqian2010@gmail.com

# 先package！！在构建docker容器，push镜像
# docker run --rm -p 18090:8090 -p 18080:8080 -p 18081:8081 jacoco-web:latest
# docker stop xx
# duqian02/Dusa0..
# docker login site.duqian.cn

# docker build -t jacoco-web .
# docker tag jacoco-web:latest site.duqian.cn/duqian02/android/jacoco-web:latest
# docker push site.duqian.cn/duqian02/android/jacoco-web:latest
# docker run --rm -p 18090:8090 -p 18080:8080 site.duqian.cn/duqian02/android/jacoco-web:latest

#cc@android
# 手动更新镜像-》android @  http://portainer.duqian.cn/#!/1/docker/stacks/jacoco?id=53&type=1&regular=true&external=false&orphaned=false

# 常见报错：
# docker未启动，ERROR: Error response from daemon: open \.\pipe\docker_engine_linux: The system cannot find the file specified.errors pretty printing info
#Net stop com.docker.service
#Net start com.docker.service