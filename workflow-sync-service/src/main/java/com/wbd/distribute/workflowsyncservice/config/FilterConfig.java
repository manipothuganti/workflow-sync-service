package com.wbd.distribute.workflowsyncservice.config;

import jakarta.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wbd.foundry.filter.CorsFilter;

@Configuration
public class FilterConfig {

    @Bean
    public Filter cors() {
        return new CorsFilter();
    }

}
