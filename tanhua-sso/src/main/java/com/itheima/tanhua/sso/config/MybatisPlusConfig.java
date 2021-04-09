package com.itheima.tanhua.sso.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.itheima.sso.mapper")
public class MybatisPlusConfig {
}
