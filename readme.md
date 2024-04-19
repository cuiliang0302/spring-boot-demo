# 项目说明

最简单的springboot项目，用于测试java项目打包部署等功能。

# 版本依赖

Openjdk:17.0.7

Maven:3.9.3

Spring Boot:3.1.1

# 打包构建

```bash
# 在项目根目录执行命令
mvn clean package
# 控制台输出如下内容表示打包成功
[INFO] Replacing main artifact /Users/cuiliang/IdeaProjects/SpringBootDemo/target/SpringBootDemo-0.0.1-SNAPSHOT.jar with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to /Users/cuiliang/IdeaProjects/SpringBootDemo/target/SpringBootDemo-0.0.1-SNAPSHOT.jar.original
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:04 min
[INFO] Finished at: 2023-07-02T10:44:22+08:00
[INFO] ------------------------------------------------------------------------
```

# 运行项目

```bash
java -jar target/SpringBootDemo-0.0.1-SNAPSHOT.jar
# 控制台输出以下内容表示运行成功
2023-07-02T10:46:55.109+08:00  INFO 5554 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8888 (http) with context path ''
2023-07-02T10:46:55.136+08:00  INFO 5554 --- [           main] c.e.s.SpringBootDemoApplication          : Started SpringBootDemoApplication in 2.288 seconds (process running for 2.76)
```

# 访问测试

```bash
➜  SpringBootDemo git:(main) ✗ curl 127.0.0.1:8888/       
Hello SpringBoot Version:v1   
➜  SpringBootDemo git:(main) ✗ curl 127.0.0.1:8888/health
ok
```

# 修改首页输出内容

修改文件：com/example/springbootdemo/HelloWorldController.java

修改内容：public String hello() {
return "Hello SpringBoot Version:v1";
}

# 构建镜像并部署
```bash
➜  SpringBootDemo git:(test) ✗ docker build -t springboot_demo:v1 .   
➜  SpringBootDemo git:(test) ✗ docker run -d -p 8888:8888 --name springboot_demo springboot_demo:v1
```