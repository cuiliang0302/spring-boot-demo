FROM openjdk:17-jdk-alpine
EXPOSE 8888
ARG JAR_FILE=target/SpringBootDemo-0.0.1-SNAPSHOT.jar
HEALTHCHECK --interval=5s --timeout=3s \
  CMD curl -fs http://127.0.0.1:8888/health || exit 1
ADD ${JAR_FILE} app.jar
CMD ["java","-jar","/app.jar"]