FROM openjdk:17-jdk-alpine
# 设置环境变量
ENV ENV_NAME=test
EXPOSE 8888
RUN apk --no-cache add curl
ARG JAR_FILE=target/SpringBootDemo-0.0.1-SNAPSHOT.jar
HEALTHCHECK --interval=5s --timeout=3s \
  CMD curl -fs http://127.0.0.1:8888/health || exit 1
ADD ${JAR_FILE} app.jar
CMD ["java","-jar","/app.jar"]