package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.validation.SignupForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class SignupControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    public void testGetSignupAlreadyLoggedIn() throws Exception {
        mvc.perform(get("/signup")
                .with(user("login0"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/main"));
    }

    @Test
    public void testGetSignupDefault() throws Exception {
        mvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attribute("signupForm", new SignupForm()));
    }

    @Test
    public void testPostSignupExistingAccount() throws Exception {
        when(passwordEncoder.encode(eq("password0"))).thenReturn("encodedPassword0");
        when(usersRepository.createNewUser(eq("login0"), eq("encodedPassword0")))
                .thenThrow(JpaSystemException.class);

        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("password0");
        signupForm.setRepeatPassword("password0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password0")
                .param("repeatPassword", "password0")
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("signupForm", signupForm))
                .andExpect(model().attributeHasFieldErrors("signupForm", "login"));
        verify(usersRepository, times(1)).createNewUser("login0", "encodedPassword0");
        verify(passwordEncoder, times(1)).encode("password0");

    }

    @Test
    public void testPostSignupInvalidLogin() throws Exception {
        for (String login : List.of("", "йцукен", "qwe", "qwertyuiop")) {
            SignupForm signupForm = new SignupForm();
            signupForm.setLogin(login);
            signupForm.setPassword("password0");
            signupForm.setRepeatPassword("password0");
            mvc.perform(post("/signup")
                    .param("login", login)
                    .param("password", "password0")
                    .param("repeatPassword", "password0")
                    .with(csrf())
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
            SignupForm signupForm = new SignupForm();
            signupForm.setLogin(login);
            signupForm.setPassword("");
            signupForm.setRepeatPassword("");
            mvc.perform(post("/signup")
                    .param("login", login)
                    .param("password", "")
                    .param("repeatPassword", "")
                    .with(csrf())
            )
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("signupForm", signupForm))
                    .andExpect(model().attributeHasFieldErrors("signupForm", "login", "password"));
        }
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupEmptyPassword() throws Exception {
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("");
        signupForm.setRepeatPassword("");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "")
                .param("repeatPassword", "")
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("signupForm", signupForm))
                .andExpect(model().attributeHasFieldErrors("signupForm", "password"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupPasswordsDoNotMatch() throws Exception {
        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("qwerty");
        signupForm.setRepeatPassword("");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "qwerty")
                .param("repeatPassword", "")
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("signupForm", signupForm))
                .andExpect(model().attributeHasErrors("signupForm"));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testPostSignupSuccess() throws Exception {
        User user = new User("login0", "encodedPassword0");
        when(passwordEncoder.encode(eq("password0"))).thenReturn("encodedPassword0");
        when(usersRepository.createNewUser(eq("login0"), eq("encodedPassword0"))).thenReturn(user);
        Authentication token = new UsernamePasswordAuthenticationToken("login0", "password0");
        when(authenticationProvider.supports(eq(token.getClass()))).thenReturn(true);
        when(authenticationProvider.authenticate(eq(token))).thenReturn(token);

        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("password0");
        signupForm.setRepeatPassword("password0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password0")
                .param("repeatPassword", "password0")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/main"));
        verify(usersRepository, times(1)).createNewUser("login0", "encodedPassword0");
        verify(passwordEncoder, times(1)).encode("password0");
    }

    @Test
    public void testPostSignupError() throws Exception {
        when(passwordEncoder.encode(eq("password0"))).thenReturn("encodedPassword0");
        when(usersRepository.createNewUser(eq("login0"), eq("encodedPassword0"))).thenThrow(RuntimeException.class);

        SignupForm signupForm = new SignupForm();
        signupForm.setLogin("login0");
        signupForm.setPassword("password0");
        signupForm.setRepeatPassword("password0");
        mvc.perform(post("/signup")
                .param("login", "login0")
                .param("password", "password0")
                .param("repeatPassword", "password0")
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", true));
        verify(usersRepository, times(1)).createNewUser("login0", "encodedPassword0");
        verify(passwordEncoder, times(1)).encode("password0");
    }

}