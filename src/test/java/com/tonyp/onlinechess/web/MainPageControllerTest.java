package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
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

import static com.tonyp.onlinechess.web.MainPageController.MAIN_PAGE_RESULTS;
import static com.tonyp.onlinechess.web.MainPageController.MAIN_PAGE_RESULTS_MOBILE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class MainPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ChallengesRepository challengesRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Mock
    private Page<Challenge> challenges;

    @Mock
    private Page<Challenge> challengesMobile;

    @Mock
    private Page<Game> games;

    @Mock
    private Page<Game> gamesMobile;

    @Test
    public void testMainChallengeCreated() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesRepository.findByToOrderByTimestampDesc(
                eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS))
        )).thenReturn(challenges);
        when(challengesRepository.findByToOrderByTimestampDesc(
                eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE))
        )).thenReturn(challengesMobile);
        when(gamesRepository.findByUser(eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS)))).thenReturn(games);
        when(gamesRepository.findByUser(eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE)))).thenReturn(gamesMobile);

        mvc.perform(get("/app/main")
                .with(user("login0"))
                .param("challenge_created", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeCreated", true))
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("incomingChallenges", challenges))
                .andExpect(model().attribute("incomingChallengesMobile", challengesMobile))
                .andExpect(model().attribute("games", games))
                .andExpect(model().attribute("gamesMobile", gamesMobile));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(challengesRepository, times(1))
                .findByToOrderByTimestampDesc(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        verify(challengesRepository, times(1))
                .findByToOrderByTimestampDesc(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
        verify(gamesRepository, times(1))
                .findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        verify(gamesRepository, times(1))
                .findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
    }

    @Test
    public void testMainChallengeAccepted() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesRepository.findByToOrderByTimestampDesc(
                eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS))
        )).thenReturn(challenges);
        when(challengesRepository.findByToOrderByTimestampDesc(
                eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE))
        )).thenReturn(challengesMobile);
        when(gamesRepository.findByUser(eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS)))).thenReturn(games);
        when(gamesRepository.findByUser(eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE)))).thenReturn(gamesMobile);

        mvc.perform(get("/app/main")
                .with(user("login0"))
                .param("challenge_accepted", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeCreated", false))
                .andExpect(model().attribute("challengeAccepted", true))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("incomingChallenges", challenges))
                .andExpect(model().attribute("incomingChallengesMobile", challengesMobile))
                .andExpect(model().attribute("games", games))
                .andExpect(model().attribute("gamesMobile", gamesMobile));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(challengesRepository, times(1))
                .findByToOrderByTimestampDesc(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        verify(challengesRepository, times(1))
                .findByToOrderByTimestampDesc(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
        verify(gamesRepository, times(1))
                .findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        verify(gamesRepository, times(1))
                .findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
    }

    @Test
    public void testMainError() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesRepository.findByToOrderByTimestampDesc(
                eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS))
        )).thenReturn(challenges);
        when(challengesRepository.findByToOrderByTimestampDesc(
                eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE))
        )).thenReturn(challengesMobile);
        when(gamesRepository.findByUser(eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS)))).thenReturn(games);
        when(gamesRepository.findByUser(eq(user), eq(PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE)))).thenReturn(gamesMobile);

        mvc.perform(get("/app/main")
                .with(user("login0"))
                .param("error", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeCreated", false))
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("incomingChallenges", challenges))
                .andExpect(model().attribute("incomingChallengesMobile", challengesMobile))
                .andExpect(model().attribute("games", games))
                .andExpect(model().attribute("gamesMobile", gamesMobile));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(challengesRepository, times(1))
                .findByToOrderByTimestampDesc(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        verify(challengesRepository, times(1))
                .findByToOrderByTimestampDesc(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
        verify(gamesRepository, times(1))
                .findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        verify(gamesRepository, times(1))
                .findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
    }

}