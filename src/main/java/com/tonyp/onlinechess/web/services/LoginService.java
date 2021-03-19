package com.tonyp.onlinechess.web.services;

import com.tonyp.onlinechess.dao.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LoginService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.tonyp.onlinechess.model.User found = usersRepository.findByLogin(username);
        if (found == null) {
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        return User.withUsername(username)
                .password(found.getPassword())
                .roles("user")
                .build();
    }

}
