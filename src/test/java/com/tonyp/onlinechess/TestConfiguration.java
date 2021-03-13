package com.tonyp.onlinechess;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.web.AppJpaConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.tonyp.onlinechess.dao")
@EnableJpaRepositories(basePackages = "com.tonyp.onlinechess.dao")
@EnableTransactionManagement
public class TestConfiguration {
    @Bean
    public EntityManager entityManager(EntityManagerFactory factory) {
        return factory.createEntityManager();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return Persistence.createEntityManagerFactory("TestDatabase");
    }

    @Bean
    public TransactionManager transactionManager(EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}