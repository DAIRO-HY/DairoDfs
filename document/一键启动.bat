@echo off

@REM 防止控制台中文乱码
chcp 65001

@REM 软件版本号，这里可以自行修改切换自己需要的版本号
set JAR_VERSION=1.0.0

@REM 下载JDK
set DAIRO_DFS_FOLDER=.\dairo-dfs
if not exist %DAIRO_DFS_FOLDER%\ (
    @REM 文件夹不存在时创建
    mkdir %DAIRO_DFS_FOLDER%
)
cd %DAIRO_DFS_FOLDER%

@REM JDK文件夹
set JDK_FOLDER=.\jdk-17.0.1
if not exist %JDK_FOLDER%\ (
    @REM 下载JDK
    curl -L -o jdk-17.0.1.zip https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_windows-x64_bin.zip

    @REM 解压JDK
    tar -xf jdk-17.0.1.zip
)

@REM jar包路径
if not exist dairo-dfs-server.jar (
    @REM 下载安装包
    curl -L -o dairo-dfs-server.jar https://github.com/DAIRO-HY/DairoDfs/releases/download/%JAR_VERSION%/dairo-dfs-server.jar
)

@REM Java可执行文件
set JAVA=%JDK_FOLDER%\bin\java

@REM 执行程序
%JAVA% -Dfile.encoding=UTF-8 -jar dairo-dfs-server.jar
pause