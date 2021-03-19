package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.tonyp.onlinechess.web.GameListPageController.PAGE_RESULTS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class GameListPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Mock
    private Page<Game> games;

    @Test
    public void testGamesFirstPageDefault() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(gamesRepository.findByUserAndOpponentLoginInput(
                eq(user), eq(""), eq(PageRequest.of(0, PAGE_RESULTS))
        )).thenReturn(games);

        mvc.perform(get("/games")
                .with(user("login0"))
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("games", games));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(gamesRepository, times(1)).findByUserAndOpponentLoginInput(
                user, "", PageRequest.of(0, PAGE_RESULTS)
        );
    }

    @Test
    public void testGamesLastPageWithSearch() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(gamesRepository.findByUserAndOpponentLoginInput(
                eq(user), eq("qwerty"), eq(PageRequest.of(3, PAGE_RESULTS))
        )).thenReturn(games);

        mvc.perform(get("/games")
                .with(user("login0"))
                .param("page", "4")
                .param("search", "qwerty")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "qwerty"))
                .andExpect(model().attribute("page", 4))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("games", games));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(gamesRepository, times(1)).findByUserAndOpponentLoginInput(
                user, "qwerty", PageRequest.of(3, PAGE_RESULTS)
        );
    }

}