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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.Cookie;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Test
    public void testGetLoginAlreadyLoggedIn() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/login")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main"));
        verifyNoInteractions(manager, usersDao);
    }

    @Test
    public void testGetLoginDefault() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("forceLogout", false))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("incorrectPassword", false));
        verifyNoInteractions(manager, usersDao);
    }

    @Test
    public void testGetLoginError() throws Exception {
        mvc.perform(get("/login")
                .param("error", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("forceLogout", false))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("incorrectPassword", false));
        verifyNoInteractions(manager, usersDao);
    }

    @Test
    public void testGetLoginForceLogout() throws Exception {
        mvc.perform(get("/login")
                .param("force_logout", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("forceLogout", true))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("incorrectPassword", false));
        verifyNoInteractions(manager, usersDao);
    }

    @Test
    public void testGetLoginIncorrectLogin() throws Exception {
        mvc.perform(get("/login")
                .param("incorrect_login", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("forceLogout", false))
                .andExpect(model().attribute("incorrectLogin", true))
                .andExpect(model().attribute("incorrectPassword", false));
        verifyNoInteractions(manager, usersDao);
    }

    @Test
    public void testGetLoginIncorrectPassword() throws Exception {
        mvc.perform(get("/login")
                .param("incorrect_password", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("forceLogout", false))
                .andExpect(model().attribute("incorrectLogin", false))
                .andExpect(model().attribute("incorrectPassword", true));
        verifyNoInteractions(manager, usersDao);
    }

    @Test
    public void testPostLoginIncorrectLogin() throws Exception {
        when(usersDao.findByLogin(eq("login0"))).thenReturn(null);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/login")
                .param("login", "login0")
                .param("password", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?incorrect_login={incorrectLogin}", "true"));
        verify(usersDao, times(1)).findByLogin("login0");
    }

    @Test
    public void testPostLoginIncorrectPassword() throws Exception {
        User user = new User("login0", "password0");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/login")
                .param("login", "login0")
                .param("password", "password1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?incorrect_password={incorrectPassword}", "true"));
        verify(usersDao, times(1)).findByLogin("login0");
    }

    @Test
    public void testPostLoginSuccess() throws Exception {
        User user = new User("login0", "password0");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/login")
                .param("login", "login0")
                .param("password", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
        verify(usersDao, times(1)).findByLogin("login0");
    }

    @Test
    public void testPostLoginError() throws Exception {
        when(usersDao.findByLogin(eq("login0"))).thenThrow(RuntimeException.class);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/login")
                .param("login", "login0")
                .param("password", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?error={error}", "true"));
        verify(usersDao, times(1)).findByLogin("login0");
    }

    @Test
    public void testLogout() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/logout")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(manager, usersDao);
    }


}