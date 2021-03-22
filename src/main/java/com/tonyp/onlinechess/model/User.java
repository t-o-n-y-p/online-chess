package com.tonyp.onlinechess.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private int id;

    @Column(unique = true, nullable = false, length = 9)
    @EqualsAndHashCode.Include
    @Pattern(regexp = "[a-zA-Z0-9]{4,9}")
    private String login;

    @Column(nullable = false, length = 60)
    @NotBlank
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private double rating;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        rating = 1200.0;
    }
}

