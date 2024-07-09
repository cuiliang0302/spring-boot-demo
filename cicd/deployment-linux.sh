#!/bin/bash
#接收外部参数
ARTIFACT_USER=$1 # artifactory用户名
ARTIFACTORY_KEY=$2 # artifactory密钥
TARGET_PATH=$3 # 文件保存路径
FILE_URL=$4 # 文件仓库路径
JAVA_CMD="java -jar $TARGET_PATH"
echo $ARTIFACT_USER
echo $TARGET_PATH
echo $FILE_URL
echo $JAVA_CMD
# 根据TARGET_PATH获取目录名
DIRECTORY=$(dirname "$TARGET_PATH")

# 检查目标目录是否存在，如果不存在就创建
if [ ! -d "$DIRECTORY" ]; then
    mkdir -p "$DIRECTORY"
    echo "目录创建完成: $DIRECTORY"
fi

# 使用curl下载文件到目标目录
curl -u$ARTIFACT_USER:$ARTIFACTORY_KEY -o "$TARGET_PATH" "$FILE_URL"
echo "文件下载完成：$TARGET_PATH"
# 获取当前运行的进程ID
PID=$(ps -aux | grep java | grep "$DIRECTORY" | grep -v grep | awk '{print $2}')

# 检查进程是否存在
if [ -z "$PID" ]; then
    echo "找不到正在运行的进程。正在启动新进程"
else
    echo "找到正在运行的进程 PID: $PID 正在停止进程"
    kill -9 $PID
    echo "进程已停止"
fi

# 启动新的进程
echo "正在启动新进程"
nohup $JAVA_CMD > /dev/null 2>&1 &
echo "新进程已启动"