package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private UsersRepository usersRepository;

    @Test
    public void testGetLoginDefault() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("forceLogout", false));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostLoginEmptyFields() throws Exception {
        mvc.perform(post("/login")
                .with(user(""))
                .param("login", "")
                .param("password", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostLoginIncorrectLogin() throws Exception {
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(null);

        mvc.perform(post("/login")
                .with(user("login0"))
                .param("login", "login0")
                .param("password", "password0")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testPostLoginIncorrectPassword() throws Exception {
        User user = new User("login0", "password1");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);

        mvc.perform(post("/login")
                .with(user("login0"))
                .param("login", "login0")
                .param("password", "password0")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testPostLoginSuccess() throws Exception {
        User user = new User("login0", "password0");
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);

        mvc.perform(post("/login")
                .with(user("login0"))
                .param("login", "login0")
                .param("password", "password0")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testPostLoginError() throws Exception {
        when(usersRepository.findByLogin(eq("login0"))).thenThrow(RuntimeException.class);

        mvc.perform(post("/login")
                .with(user("login0"))
                .param("login", "login0")
                .param("password", "password0")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
        verify(usersRepository, times(1)).findByLogin("login0");
    }


}