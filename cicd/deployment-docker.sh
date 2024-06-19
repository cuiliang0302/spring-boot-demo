#!/bin/bash
# 接收外部参数
HARBOR_USERNAME=$1 # harbor账号
HARBOR_PASSWORD=$2 # harbor密码
IMAGE_NAME=$3 # 镜像名称
APP_NAME=$4 # 容器名称
HARBOR_URL=$(echo "$IMAGE_NAME" | cut -d'/' -f1) # 仓库地址
# 检查容器是否在运行
if docker ps -a --format '{{.Names}}' | grep -q "^$APP_NAME$"; then
    echo "容器$APP_NAME 正在运行中."

    # 停止容器
    echo "停止容器$APP_NAME..."
    docker stop $APP_NAME > /dev/null

    # 删除容器
    echo "删除容器$APP_NAME..."
    docker rm $APP_NAME > /dev/null

    # 删除镜像
    echo "删除镜像$APP_NAME..."
    docker rmi -f $(docker images --format '{{.Repository}}:{{.Tag}}' | grep "^$APP_NAME") > /dev/null

    echo "容器$APP_NAME已停止并成功删除镜像."
else
    echo "容器$APP_NAME未运行."
fi

# 拉取镜像并启动容器
echo "开始登录harbor仓库并拉取镜像$IMAGE_NAME..."
docker login -u $HARBOR_USERNAME -p $HARBOR_PASSWORD $HARBOR_URL
echo "开始启动容器$APP_NAME..."
docker run -d --name $APP_NAME -p 8888:8888 $IMAGE_NAME