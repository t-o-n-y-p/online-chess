package com.tonyp.onlinechess.web;

public class UserSession {

    private String login;

    public UserSession() {
    }

    public UserSession(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
