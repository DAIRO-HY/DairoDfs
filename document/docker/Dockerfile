FROM ubuntu:24.10
RUN apt update

#ubuntu镜像中默认不带curl,安装curl
RUN yes|apt install curl
RUN mkdir /app
WORKDIR /app

#下载启动脚本
ADD https://github.com/DAIRO-HY/DairoDfs/raw/main/document/%E4%B8%80%E9%94%AE%E5%90%AF%E5%8A%A8/Linux/start.sh /app

#授权可执行
RUN chmod +x start.sh

#启动脚本
ENTRYPOINT ["./start.sh"]