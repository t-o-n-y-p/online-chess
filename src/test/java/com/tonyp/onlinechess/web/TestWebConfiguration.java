package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.MovesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.concurrent.ExecutionException;

@Configuration
@ComponentScan(basePackages = "com.tonyp.onlinechess.web", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                AppJpaConfiguration.class, OnlineChessApplication.class
        })
})
public class TestWebConfiguration {

    @MockBean
    private EntityManager manager;

    @MockBean
    private EntityTransaction tx;

    @MockBean
    private ChallengesDao challengesDao;

}
