# 项目说明

最简单的springboot项目，用于测试java项目使用jenkins、gitlab ci、argo cd打包部署功能。

# 版本依赖

Openjdk:17.0.7

Maven:3.9.3

Spring Boot:3.1.1

# 本地打包与测试

## 打包项目

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

## 运行项目

```bash
java -jar target/SpringBootDemo-0.0.1-SNAPSHOT.jar
# 控制台输出以下内容表示运行成功
2023-07-02T10:46:55.109+08:00  INFO 5554 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8888 (http) with context path ''
2023-07-02T10:46:55.136+08:00  INFO 5554 --- [           main] c.e.s.SpringBootDemoApplication          : Started SpringBootDemoApplication in 2.288 seconds (process running for 2.76)
```

## 访问测试

```bash
➜  SpringBootDemo git:(main) ✗ curl 127.0.0.1:8888/       
Hello SpringBoot Version:v1   
➜  SpringBootDemo git:(main) ✗ curl 127.0.0.1:8888/health
ok
```

## 修改首页输出内容

修改文件：com/example/springbootdemo/HelloWorldController.java

修改内容：public String hello() {
return "Hello SpringBoot Version:v1";
}

## 构建docker镜像并部署
```bash
➜  SpringBootDemo git:(main) ✗ docker build -t springboot_demo:v1 .   
➜  SpringBootDemo git:(main) ✗ docker run -d -p 8888:8888 --name springboot_demo springboot_demo:v1
```

# CI/CD

## 参考文档

https://www.cuiliangblog.cn/catalog/1939987

## 文件目录介绍

```bash
cicd
├── Dockerfile  # 项目打包成docker镜像的文本文件
├── Dockerfile-maven # 自定义maven镜像，替换国内源
├── deployment-docker.sh # 部署到docker环境脚本
├── deployment-linux.sh # 部署到linux系统环境脚本
├── gitlab-ci # gitlab ci/cd流水线
│   ├── docker.yml # 部署到docker环境完整流水线
│   ├── k8s.yml # 部署到k8s环境完整流水线
│   └── linux.yml # 部署到linux系统环境完整流水线
├── jenkins # jenkins ci/cd流水线
│   ├── Dockerfile-jenkins-slave # 自定义jenkins slave镜像的文本文件
│   ├── Jenkinsfile-docker.groovy # jenkins发布到docker环境的完整流水线
│   ├── Jenkinsfile-k8s.groovy # jenkins发布到k8s环境的完整流水线
│   └── email.html # jenkins发送自定义邮件格式代码
├── jmeter # jmeter接口自动化测试
│   └── demo.jmx # 接口自动化测试配置文件
├── k8s.yaml # 部署到k8s的yaml文件
└── sonar-project.properties # SonarQube代码扫描配置文件

```
## 自动化测试

jmeter目录下存放示例接口自动化测试脚本，主要测试内容如下

- http://www.baidu.com/
- http://demo:8888/
- http://demo.local.com/

如果需要自动化测试，在服务部署后使用域名或者主机名+端口方式访问测试