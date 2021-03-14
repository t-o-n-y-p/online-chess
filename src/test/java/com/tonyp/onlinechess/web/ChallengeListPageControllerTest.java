package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
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

import static com.tonyp.onlinechess.web.ChallengeListPageController.PAGE_RESULTS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class ChallengeListPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ChallengesRepository challengesRepository;

    @Mock
    private Page<Challenge> challenges;

    @Test
    public void testChallengesNotLoggedIn() throws Exception {
        mvc.perform(get("/challenges"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository, challengesRepository);
    }

    @Test
    public void testChallengesFirstPageDefault() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesRepository.findIncomingChallengesByOpponentLoginInput(
                eq(user), eq(""), eq(PageRequest.of(0, PAGE_RESULTS))
        )).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challenges", challenges));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(challengesRepository, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "", PageRequest.of(0, PAGE_RESULTS)
        );
    }

    @Test
    public void testChallengesSecondPageWithChallengeAccepted() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesRepository.findIncomingChallengesByOpponentLoginInput(
                eq(user), eq(""), eq(PageRequest.of(1, PAGE_RESULTS))
        )).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .param("page", "2")
                .param("challenge_accepted", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", true))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 2))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challenges", challenges));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(challengesRepository, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "", PageRequest.of(1, PAGE_RESULTS)
        );
    }

    @Test
    public void testChallengesLastPageWithSearchAndErrorFlag() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesRepository.findIncomingChallengesByOpponentLoginInput(
                eq(user), eq("qwerty"), eq(PageRequest.of(3, PAGE_RESULTS))
        )).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .param("page", "4")
                .param("search", "qwerty")
                .param("error", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("search", "qwerty"))
                .andExpect(model().attribute("page", 4))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challenges", challenges));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(challengesRepository, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "qwerty", PageRequest.of(3, PAGE_RESULTS)
        );
    }
}