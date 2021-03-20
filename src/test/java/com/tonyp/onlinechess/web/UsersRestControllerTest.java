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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class UsersRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    public void testFindByLoginSuccess() throws Exception {
        User user = new User("login0", "password0");
        user.setId(1);
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(user);

        assertEquals("{\"id\":1,\"login\":\"login0\",\"rating\":1200.0}",
                mvc.perform(get("/api/user/login0"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );
    }

    @Test
    public void testFindByLoginNoUser() throws Exception {
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(null);

        mvc.perform(get("/api/user/login0"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testFindByLoginError() throws Exception {
        when(usersRepository.findByLogin(eq("login0"))).thenThrow(RuntimeException.class);

        mvc.perform(get("/api/user/login0"))
                .andExpect(status().isInternalServerError());
    }

}