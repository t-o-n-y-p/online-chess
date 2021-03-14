package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class ChallengeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private ChallengesRepository challengesRepository;

    @Test
    public void testAcceptIsNotLoggedIn() throws Exception {
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("../login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository, gamesRepository, challengesRepository);
    }

    @Test
    public void testAcceptFromMainWhite() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.WHITE);
        when(challengesRepository.findById(eq(1))).thenReturn(Optional.of(challenge));

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?challenge_accepted={accepted}", "true"));

        verify(challengesRepository, times(1)).findById(1);
        verify(challengesRepository, times(1)).delete(challenge);
        verify(gamesRepository, times(1)).createNewGame(to, from);
    }

    @Test
    public void testAcceptFromChallengesBlack() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.BLACK);
        when(challengesRepository.findById(eq(1))).thenReturn(Optional.of(challenge));

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .param("from_challenges", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate(
                        "/challenges?challenge_accepted={accepted}&page={page}", "true", "1"));

        verify(challengesRepository, times(1)).findById(1);
        verify(challengesRepository, times(1)).delete(challenge);
        verify(gamesRepository, times(1)).createNewGame(from, to);
    }

    @Test
    public void testAcceptFromChallengesToPreviousPageWhite() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.WHITE);
        when(challengesRepository.findById(eq(1))).thenReturn(Optional.of(challenge));

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .param("page", "3")
                .param("to_previous_page", "true")
                .param("from_challenges", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate(
                        "/challenges?challenge_accepted={accepted}&page={page}", "true", "2"));

        verify(challengesRepository, times(1)).findById(1);
        verify(challengesRepository, times(1)).delete(challenge);
        verify(gamesRepository, times(1)).createNewGame(to, from);
    }

    @Test
    public void testAcceptErrorBlack() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.BLACK);
        when(challengesRepository.findById(eq(1))).thenReturn(Optional.of(challenge));
        when(gamesRepository.createNewGame(eq(from), eq(to))).thenThrow(RuntimeException.class);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?error={error}", "true"));

        verify(challengesRepository, times(1)).findById(1);
        verify(challengesRepository, times(1)).delete(challenge);
        verify(gamesRepository, times(1)).createNewGame(from, to);
    }

    @Test
    public void testCreateIsNotLoggedIn() throws Exception {
        mvc.perform(post("/challenge")
                .param("opponent_id", "1")
                .param("target_color", Color.WHITE.name())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository, gamesRepository, challengesRepository);
    }

    @Test
    public void testCreateSuccessWhite() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(from);
        when(usersRepository.findById(eq(1))).thenReturn(Optional.of(to));

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/challenge")
                .param("opponent_id", "1")
                .param("target_color", Color.WHITE.name())
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?challenge_created={created}", "true"));
        verify(usersRepository, times(1)).findById(1);
        verify(challengesRepository, times(1)).createNewChallenge(from, to, Color.WHITE);
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testCreateErrorBlack() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(from);
        when(usersRepository.findById(eq(1))).thenReturn(Optional.of(to));
        when(challengesRepository.createNewChallenge(eq(from), eq(to), eq(Color.BLACK))).thenThrow(RuntimeException.class);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/challenge")
                .param("opponent_id", "1")
                .param("target_color", Color.BLACK.name())
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?error={error}", "true"));
        verify(usersRepository, times(1)).findById(1);
        verify(challengesRepository, times(1)).createNewChallenge(from, to, Color.BLACK);
        verify(usersRepository, times(1)).findByLogin("login0");
    }
}