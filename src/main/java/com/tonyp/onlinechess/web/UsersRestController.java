package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.User;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class UsersRestController {

    private final UsersRepository usersRepository;

    @GetMapping("/api/user/{login}")
    public ResponseEntity<?> getUser(@PathVariable String login) {
        try {
            User found = usersRepository.findByLogin(login);
            return found == null ? new ResponseEntity<Void>(HttpStatus.NO_CONTENT)
                    : new ResponseEntity<>(found, HttpStatus.OK);
        } catch (Throwable e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred. Please try again later.",
                    e
            );
        }
    }

}
