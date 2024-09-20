FROM ubuntu:24.10
RUN apt update

#ubuntu镜像中默认不带curl,安装curl
RUN yes|apt install curl
RUN mkdir /app
WORKDIR /app

#下载启动脚本
ADD https://github.com/DAIRO-HY/DairoDfs/raw/main/document/quick-start/Linux/start.sh /app

#授权可执行
RUN chmod +x start.sh

# 定义运行时的环境变量
ENV VERSION=1.0.0

#启动脚本
ENTRYPOINT "./start.sh" $VERSION