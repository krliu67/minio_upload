package com.example.demot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.example.demot"})
@MapperScan(basePackages = {"com.example.demot.mapper"},markerInterface = BaseMapper.class)
public class DemotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemotApplication.class, args);
    }

}
