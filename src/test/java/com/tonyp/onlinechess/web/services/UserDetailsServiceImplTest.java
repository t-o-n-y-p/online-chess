package com.tonyp.onlinechess.web.services;

import com.tonyp.onlinechess.dao.UsersRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UserDetailsServiceImpl.class)
public class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    private UsersRepository usersRepository;

    @Test
    public void testExistingUser() {
        com.tonyp.onlinechess.model.User user
                = new com.tonyp.onlinechess.model.User("login0", "encryptedPassword0");
        when(usersRepository.findByLogin("login0")).thenReturn(user);

        UserDetails expectedResult = User.withUsername("login0")
                .password("encryptedPassword0")
                .roles("user")
                .build();
        UserDetails actualResult = userDetailsServiceImpl.loadUserByUsername("login0");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testNonExistingUser() {
        Assert.assertThrows(UsernameNotFoundException.class, () -> userDetailsServiceImpl.loadUserByUsername("login0"));
    }

}