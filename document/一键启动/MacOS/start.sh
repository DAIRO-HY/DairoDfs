#!/bin/bash

# 获取当前脚本所在目录
SCRIPT_DIR="$(dirname "$0")"
cd $SCRIPT_DIR

JAR_VERSION="1.0.0"
DATA_FOLDER="./DairoDfs"

#创建DairoDfs文件夹
if [ ! -d "$DATA_FOLDER" ]; then
  mkdir -p "$DATA_FOLDER"
fi

#进入文件夹
cd $DATA_FOLDER

if [ ! -d "jdk-17.0.1.jdk" ]; then

  #下载JDK
  echo "正在下载JDK"

  #获取CPU信息
  architecture=$(uname -m)
  if [[ "$architecture" == "x86_64" ]]; then
      echo "CPU is x64 (x86_64)"
      JDK_URL="https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_macos-x64_bin.tar.gz"
  elif [[ "$architecture" == "arm64" || "$architecture" == "aarch64" ]]; then
      echo "CPU is ARM (arm64/aarch64)"
      JDK_URL="https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_macos-aarch64_bin.tar.gz"
  else
      echo "Unknown CPU architecture: $architecture"
      exit 1
  fi
  curl -L -o jdk.tar.gz $JDK_URL
  echo "JDK下载完成"
  echo "正在解压JDK"

  #将JDK解压到临时文件夹
  mkdir dairo.temp
  tar -xzf jdk.tar.gz -C dairo.temp
  mv dairo.temp/* .
  rm -r dairo.temp
  echo "JDK安装完成"
fi

if [ ! -f "dairo-dfs-server.jar" ]; then
  echo "正在下载dairo-dfs-server.jar"
  curl -L -o dairo-dfs-server.jar https://github.com/DAIRO-HY/DairoDfs/releases/download/$JAR_VERSION/dairo-dfs-server.jar
  echo "dairo-dfs-server.jar下载完成"
fi
./jdk-17.0.1.jdk/Contents/Home/bin/java -jar dairo-dfs-server.jar