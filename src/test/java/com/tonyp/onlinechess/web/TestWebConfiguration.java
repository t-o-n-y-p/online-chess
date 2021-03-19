package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.tonyp.onlinechess.web", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                AppJpaConfiguration.class, OnlineChessApplication.class, SecurityConfiguration.class
        })
})
@EnableWebSecurity
public class TestWebConfiguration {

    @MockBean
    private ChallengesRepository challengesRepository;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private GamesRepository gamesRepository;

    @MockBean
    private MovesRepository movesRepository;

    @MockBean
    private UserDetailsService userDetailsService;

}
