#!/bin/bash
date >> /tmp/date.txt
#接收外部参数
harbor_username=$1
harbor_password=$2
image_name=$3
image_app=$4
echo "$harbor_username" >> /tmp/date.txt
echo "$harbor_password" >> /tmp/date.txt
echo "$image_name" >> /tmp/date.txt
echo "$image_app" >> /tmp/date.txt
# 检查容器是否在运行
if docker ps -a --format '{{.Names}}' | grep -q "^$image_app$"; then
    echo "容器 $image_app 正在运行中."

    # 停止容器
    echo "停止容器 $image_app..."
    docker stop $image_app > /dev/null

    # 删除容器
    echo "删除容器 $image_app..."
    docker rm $image_app > /dev/null

    # 删除镜像
    echo "删除镜像 $image_app..."
    docker rmi -f $(docker images --format '{{.Repository}}:{{.Tag}}' | grep "^$image_app") > /dev/null

    echo "容器 $image_app 已停止并成功删除镜像."
else
    echo "容器 $image_app 未运行."
fi

# 拉取镜像并启动容器
echo "开始登录harbor仓库并拉取镜像 $image_name..."
docker login -u $harbor_username -p $harbor_password https://harbor.local.com
echo "开始启动容器 $image_app..."
docker run -d --name $image_app -p 8888:8888 $image_name