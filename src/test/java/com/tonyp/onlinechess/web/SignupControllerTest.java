package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.validation.SignupForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class SignupControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    public void testGetSignupAlreadyLoggedIn() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/signup")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testGetSignupDefault() throws Exception {
        mvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attribute("signupForm", new SignupForm()));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupExistingAccount() throws Exception {
        when(usersRepository.createNewUser(eq("login0"), eq("password0"))).thenThrow(JpaSystemException.class);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("password0");
        signupForm.setRepeatPassword("password0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password0")
                .param("repeatPassword", "password0")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("signupForm", signupForm))
                .andExpect(model().attributeHasFieldErrors("signupForm", "login"));
        verify(usersRepository, times(1)).createNewUser("login0", "password0");
    }

    @Test
    public void testPostSignupInvalidLogin() throws Exception {
        for (String login : List.of("", "йцукен", "qwe", "qwertyuiop")) {
            UserSession userSession = new UserSession();
            userSession.setLogin(login);
            SignupForm signupForm = new SignupForm();
            signupForm.setLogin(login);
            signupForm.setPassword("password0");
            signupForm.setRepeatPassword("password0");
            mvc.perform(post("/signup")
                    .param("login", login)
                    .param("password", "password0")
                    .param("repeatPassword", "password0")
                    .sessionAttr("user-session", userSession)
            )
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("signupForm", signupForm))
                    .andExpect(model().attributeHasFieldErrors("signupForm", "login"));
        }
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupInvalidLoginAndEmptyPassword() throws Exception {
        for (String login : List.of("", "йцукен", "qwe", "qwertyuiop")) {
            UserSession userSession = new UserSession();
            userSession.setLogin(login);
            SignupForm signupForm = new SignupForm();
            signupForm.setLogin(login);
            signupForm.setPassword("");
            signupForm.setRepeatPassword("");
            mvc.perform(post("/signup")
                    .param("login", login)
                    .param("password", "")
                    .param("repeatPassword", "")
                    .sessionAttr("user-session", userSession)
            )
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("signupForm", signupForm))
                    .andExpect(model().attributeHasFieldErrors("signupForm", "login", "password"));
        }
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupEmptyPassword() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("");
        signupForm.setRepeatPassword("");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "")
                .param("repeatPassword", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("signupForm", signupForm))
                .andExpect(model().attributeHasFieldErrors("signupForm", "password"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupPasswordsDoNotMatch() throws Exception {
        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("qwerty");
        signupForm.setRepeatPassword("");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "qwerty")
                .param("repeatPassword", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("signupForm", signupForm))
                .andExpect(model().attributeHasErrors("signupForm"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupSuccess() throws Exception {
        User user = new User("login0", "password0");
        when(usersRepository.createNewUser(eq("login0"), eq("password0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("password0");
        signupForm.setRepeatPassword("password0");
        mvc.perform(post("/signup")
                .flashAttr("signupForm", signupForm)
                .flashAttr("bindingResult", new BeanPropertyBindingResult(signupForm, "signupForm"))
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
        verify(usersRepository, times(1)).createNewUser("login0", "password0");
    }

    @Test
    public void testPostSignupError() throws Exception {
        when(usersRepository.createNewUser(eq("login0"), eq("password0"))).thenThrow(RuntimeException.class);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("password0");
        signupForm.setRepeatPassword("password0");
        mvc.perform(post("/signup")
                .flashAttr("signupForm", signupForm)
                .flashAttr("bindingResult", new BeanPropertyBindingResult(signupForm, "signupForm"))
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", true));
        verify(usersRepository, times(1)).createNewUser("login0", "password0");
    }

}