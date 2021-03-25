package com.tonyp.onlinechess;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.tonyp.onlinechess.dao")
@EnableJpaRepositories(basePackages = "com.tonyp.onlinechess.dao")
@EnableTransactionManagement
@EntityScan("com.tonyp.onlinechess.model")
public class TestConfiguration {
}