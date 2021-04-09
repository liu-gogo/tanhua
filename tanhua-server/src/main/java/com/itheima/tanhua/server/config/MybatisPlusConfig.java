package com.itheima.tanhua.server.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.itheima.itheima.server.mapper")
public class MybatisPlusConfig {
}
