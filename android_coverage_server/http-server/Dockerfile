FROM node:14.15.4
MAINTAINER 杜小菜 duqian2010@gmail.com
#WORKDIR 共享的磁盘目录也是这个
VOLUME /dq-coverage
#COPY <src> <dest>  只复制目录中的内容而不包含目录自身
#拷贝当前项目dq-coverage目录下的文件到磁盘中的dq-coverage目录
#ADD ./dq-coverage ./
#全部拷贝
ADD . .
#ADD index2.html index2.html
RUN npm install http-server -g
EXPOSE 8080
CMD ["http-server"]
#CMD ["http-server","dq-coverage","-p","8080"]