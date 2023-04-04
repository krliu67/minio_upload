package com.example.demot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@MapperScan("com.example.demot.mapper")
public class DemotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemotApplication.class, args);
    }

}
