FROM ubuntu:22.04

RUN mkdir /app
RUN mkdir /app/dairo-dfs
WORKDIR /app/dairo-dfs

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

COPY dairo-dfs-server.jar /app/dairo-dfs-server.jar
WORKDIR /app
#COPY application-prod.yml /app/application-prod.yml

ENTRYPOINT ["java","-jar","dairo-dfs-server.jar"]