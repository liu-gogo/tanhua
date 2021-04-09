package com.itheima.tanhua;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class}) //排除mongo的自动配置

@MapperScan("com.itheima.tanhua.sso.mapper")
public class TanhuaSsoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TanhuaSsoApplication.class, args);
    }

}
