package com.example.air;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling; // 1. 引入这个

@SpringBootApplication
@MapperScan("com.example.air.mapper")
@EnableScheduling // 2. 加上这个注解，开启定时任务
public class AirApplication {
    public static void main(String[] args) {
        SpringApplication.run(AirApplication.class, args);
    }
}