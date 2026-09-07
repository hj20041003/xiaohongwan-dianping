package com.hmdp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hmdp.client")
@MapperScan("com.hmdp.mapper")
public class HmdpApplication {
    public static void main(String[] args) {
        SpringApplication.run(HmdpApplication.class, args);
    }
}
