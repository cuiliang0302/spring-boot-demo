#!/bin/bash
#接收外部参数
HARBOR_USERNAME=$1
HARBOR_PASSWORD=$2
IMAGE_NAME=$3
IMAGE_APP=$4
# 检查容器是否在运行
if docker ps -a --format '{{.Names}}' | grep -q "^$IMAGE_APP$"; then
    echo "容器$IMAGE_APP 正在运行中."

    # 停止容器
    echo "停止容器$IMAGE_APP..."
    docker stop $IMAGE_APP > /dev/null

    # 删除容器
    echo "删除容器$IMAGE_APP..."
    docker rm $IMAGE_APP > /dev/null

    # 删除镜像
    echo "删除镜像$IMAGE_APP..."
    docker rmi -f $(docker images --format '{{.Repository}}:{{.Tag}}' | grep "^$IMAGE_APP") > /dev/null

    echo "容器$IMAGE_APP已停止并成功删除镜像."
else
    echo "容器$IMAGE_APP未运行."
fi

# 拉取镜像并启动容器
echo "开始登录harbor仓库并拉取镜像$IMAGE_NAME..."
docker login -u $HARBOR_USERNAME -p $HARBOR_PASSWORD https://harbor.local.com
echo "开始启动容器$IMAGE_APP..."
docker run -d --name $IMAGE_APP -p 8888:8888 $IMAGE_NAME