package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

@Repository
public class UsersDao {

    private EntityManager manager;

    public UsersDao(@Autowired EntityManager manager) {
        this.manager = manager;
    }

    public User createNewUser(String login, String password) {
        User newUser = new User(login, password);
        manager.persist(newUser);
        return newUser;
    }

    public User findByLogin(String login) {
        try {
            return manager.createQuery("from User where login = :login", User.class)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException notFound) {
            return null;
        }
    }

    public User findByLoginAndPassword(String login, String password) {
        try {
            return manager.createQuery("from User where login = :login and password = :password", User.class)
                    .setParameter("login", login)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException notFound) {
            return null;
        }
    }

    public List<User> findOpponentsByRating(User user, double rating, double threshold, int offset, int limit) {
        return manager.createQuery(
                "from User where rating >= :start and rating <= :end and id != :id " +
                        "order by rating desc", User.class)
                .setParameter("start", rating - threshold)
                .setParameter("end", rating + threshold)
                .setParameter("id", user.getId())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<User> findOpponentsByRatingAndLoginInput
            (User user, String input, double rating, double threshold, int offset, int limit) {
        return manager.createQuery(
                "from User where login like concat('%', :input, '%') and rating >= :start and rating <= :end " +
                        "and id != :id order by rating desc", User.class)
                .setParameter("input", input)
                .setParameter("start", rating - threshold)
                .setParameter("end", rating + threshold)
                .setParameter("id", user.getId())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

}

