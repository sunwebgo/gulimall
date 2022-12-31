package com.xha.gulimall.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCommonApplication.class, args);
    }

}
