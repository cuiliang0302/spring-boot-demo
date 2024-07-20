package com.example.springbootdemo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloWorldController {
    @RequestMapping("/")
    @ResponseBody
    public String hello() {
        // 获取环境变量 ENV_NAME，如果不存在则使用默认值 "default"
        String envName = System.getenv().getOrDefault("ENV_NAME", "default");
        return String.format("<h1>Hello SpringBoot</h1><p>Version:v1 Env:%s</p>", envName);
    }
    @RequestMapping("/health")
    @ResponseBody
    public String healthy() {
        return "ok";
    }
}
