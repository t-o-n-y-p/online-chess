package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
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

import java.util.Optional;

import static com.tonyp.onlinechess.web.ChallengePageController.PAGE_RESULTS;
import static com.tonyp.onlinechess.web.ChallengePageController.RATING_THRESHOLD;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class ChallengePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Mock
    private Page<User> opponents;

    @Test
    public void testStep1NotLoggedIn() throws Exception {
        mvc.perform(get("/challenge/step1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("../login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testStep1FirstPageDefault() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(usersRepository.findOpponentsByRatingAndLoginInput(
                eq(user), eq(""), eq(user.getRating() - RATING_THRESHOLD),
                eq(user.getRating() + RATING_THRESHOLD), eq(PageRequest.of(0, PAGE_RESULTS))
        )).thenReturn(opponents);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("opponents", opponents));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(usersRepository, times(1)).findOpponentsByRatingAndLoginInput(
                user, "", user.getRating() - RATING_THRESHOLD,
                user.getRating() + RATING_THRESHOLD, PageRequest.of(0, PAGE_RESULTS)
        );
    }

    @Test
    public void testStep1LastPageWithSearch() throws Exception {
        User user = new User("login0", "pass0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);
        when(usersRepository.findOpponentsByRatingAndLoginInput(
                eq(user), eq("qwerty"), eq(user.getRating() - RATING_THRESHOLD),
                eq(user.getRating() + RATING_THRESHOLD), eq(PageRequest.of(3, PAGE_RESULTS))
        )).thenReturn(opponents);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step1")
                .param("page", "4")
                .param("search", "qwerty")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "qwerty"))
                .andExpect(model().attribute("page", 4))
                .andExpect(model().attribute("opponents", opponents));
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(usersRepository, times(1)).findOpponentsByRatingAndLoginInput(
                user, "qwerty", user.getRating() - RATING_THRESHOLD,
                user.getRating() + RATING_THRESHOLD, PageRequest.of(3, PAGE_RESULTS)
        );
    }

    @Test
    public void testStep2NotLoggedIn() throws Exception {
        mvc.perform(get("/challenge/step2")
                .param("opponent_id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("../login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testStep2Success() throws Exception {
        User opponent = new User("login1", "pass1");
        when(usersRepository.findById(eq(1))).thenReturn(Optional.of(opponent));

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step2")
                .param("opponent_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("opponent", opponent));
        verify(usersRepository, times(1)).findById(1);
    }

}