package com.tonyp.onlinechess;

import com.tonyp.onlinechess.web.AppJpaConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Configuration
@ComponentScan(basePackages = "com.tonyp.onlinechess.dao")
public class TestConfiguration {
    @Bean
    public EntityManager entityManager(EntityManagerFactory factory) {
        return factory.createEntityManager();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return Persistence.createEntityManagerFactory("ProductionDatabase");
    }
}