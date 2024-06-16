#!/bin/bash
date >> /tmp/date.txt
#接收外部参数
ARTIFACTORY_KEY=$1
TARGET_DIR=$2
FILE_URL=$3
FILE_NAME=$4
JAVA_CMD="java -jar $TARGET_DIR/$FILE_NAME"

# 检查目标目录是否存在，如果不存在就创建
if [ ! -d "$TARGET_DIR" ]; then
    mkdir -p "$TARGET_DIR"
fi

# 使用curl下载文件到目标目录
curl -uadmin:$ARTIFACTORY_KEY -o "$TARGET_DIR/$(basename $FILE_URL)" "$FILE_URL"

echo "下载完成：$TARGET_DIR/$(basename $FILE_URL)"

# 获取当前运行的进程ID
PID=$(ps -aux | grep java | grep "$TARGET_DIR" | grep -v grep | awk '{print $2}')

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
sleep 10
ss -tunlp | grep java