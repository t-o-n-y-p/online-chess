package com.tonyp.onlinechess.web;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.tonyp.onlinechess.dao")
@EnableJpaRepositories(basePackages = "com.tonyp.onlinechess.dao")
@EnableTransactionManagement
public class AppJpaConfiguration {
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return Persistence.createEntityManagerFactory("ProductionDatabase");
    }

    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}
