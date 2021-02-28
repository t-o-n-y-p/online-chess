package com.tonyp.onlinechess.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineChessApplication {

    public static final String USER_LOGIN = "login0";

    public static void main(String[] args) {
        SpringApplication.run(OnlineChessApplication.class, args);
    }

}
