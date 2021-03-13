package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@Configuration
@ComponentScan(basePackages = "com.tonyp.onlinechess.web", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                AppJpaConfiguration.class, OnlineChessApplication.class
        })
})
public class TestWebConfiguration {

    @MockBean
    private ChallengesRepository challengesRepository;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private GamesRepository gamesRepository;

    @MockBean
    private MovesRepository movesRepository;

}
