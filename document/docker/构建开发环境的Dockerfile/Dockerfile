FROM ubuntu:22.04

RUN apt update

#安装解压工具
RUN yes|apt-get install xz-utils

RUN mkdir /app
WORKDIR /app

#####################################################################安装LibRaw#######################################

#下载Libraw,官方网址:https://www.libraw.org/download
ADD https://www.libraw.org/data/LibRaw-0.21.2.tar.gz LibRaw-0.21.2.tar.gz
RUN tar -xzf LibRaw-0.21.2.tar.gz
WORKDIR /app/LibRaw-0.21.2

#构建工具和依赖项
RUN yes|apt install build-essential cmake

#构建和安装 LibRaw
RUN ./configure
RUN make
RUN make install

#安装完成之后,执行dcraw_emu -v有可能报错
#dcraw_emu: error while loading shared libraries: libraw.so.23: cannot open shared object file: No such file or directory
#解决方案是执行ldconfig
RUN ldconfig

#删除安装源码
RUN rm /app/LibRaw-0.21.2.tar.gz
RUN rm -rf /app/LibRaw-0.21.2

#至此,Libraw安装完成,可以使用dcraw_emu -T tt.cr3指令将图片转换成tiff格式了

#####################################################################安装FFMPEG#######################################
WORKDIR /usr/local
#下载安装包https://johnvansickle.com/ffmpeg/
ADD https://www.johnvansickle.com/ffmpeg/old-releases/ffmpeg-6.0.1-amd64-static.tar.xz ./ffmpeg-release-amd64-static.tar.xz
#ADD ffmpeg-release-amd64-static.tar.xz /usr/local

#解压
RUN tar -xvf ffmpeg-release-amd64-static.tar.xz
RUN mv ffmpeg-6.0.1-amd64-static/ffmpeg /usr/local/bin/
RUN mv ffmpeg-6.0.1-amd64-static/ffprobe /usr/local/bin/

#删除安装包
RUN rm ffmpeg-release-amd64-static.tar.xz
RUN rm -rf ffmpeg-6.0.1-amd64-static



#####################################################################安装JDK#######################################
WORKDIR /usr/local
# openjdk官方下载地址https://jdk.java.net/archive/
ADD https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_linux-x64_bin.tar.gz /usr/local/openjdk.tar.gz

RUN tar -zvxf openjdk.tar.gz
RUN rm -rf openjdk.tar.gz


# 配置JAVA_HOME环境变量
ENV JAVA_HOME /usr/local/jdk-17.0.1/

# 将JAVA_HOME/bin 添加至PATH环境变量
ENV PATH $JAVA_HOME/bin:$PATH
##################################################################环境配置完成#######################################

##ADD http://git.hy-1.cn/zhoulq/gl-dfs/-/raw/master/gl-dfs-server/bootjar/gl-dfs-server-0.0.1.jar /app/gl-dfs-server.jar
COPY dairo-dfs-server.jar /app/dairo-dfs-server.jar
WORKDIR /app
#COPY application-prod.yml /app/application-prod.yml

ENTRYPOINT ["java","-jar","dairo-dfs-server.jar"]