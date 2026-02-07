package com.example.backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.example.backend.project.mapper", "com.example.backend.dict.mapper"})
public class MybatisPlusConfig {
}

