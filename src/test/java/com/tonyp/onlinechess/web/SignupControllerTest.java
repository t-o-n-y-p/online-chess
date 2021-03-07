package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class SignupControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testGetSignupAlreadyLoggedIn() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/signup")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main"));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testGetSignupDefault() throws Exception {
        mvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("invalidLogin", false));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testGetSignupError() throws Exception {
        mvc.perform(get("/signup")
                .param("error", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("invalidLogin", false));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testGetSignupIncorrectLogin() throws Exception {
        mvc.perform(get("/signup")
                .param("incorrect_login", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("incorrectLogin", true))
                .andExpect(model().attribute("invalidLogin", false));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testGetSignupInvalidLogin() throws Exception {
        mvc.perform(get("/signup")
                .param("invalid_login", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("invalidLogin", true));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testPostSignupExistingAccount() throws Exception {
        User user = new User("login0", "password0");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/signup?incorrect_login={incorrectLogin}", "true"));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(manager, times(1)).getTransaction();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testPostSignupInvalidLogin() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("йцукен");
        mvc.perform(post("/signup")
                .param("login", "йцукен")
                .param("password", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/signup?invalid_login={invalidLogin}", "true"));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testPostSignupSuccess() throws Exception {
        User user = new User("login0", "password0");
        when(usersDao.createNewUser(eq("login0"), eq("password0"))).thenReturn(user);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testPostSignupError() throws Exception {
        User user = new User("login0", "password0");
        when(usersDao.createNewUser(eq("login0"), eq("password0"))).thenReturn(user);
        when(manager.getTransaction()).thenReturn(tx);
        doThrow(RuntimeException.class).when(tx).commit();
        when(tx.isActive()).thenReturn(true);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/signup?error={error}", "true"));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(manager, times(4)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
        verify(tx, times(1)).rollback();
    }

}