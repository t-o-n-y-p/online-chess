package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    @Transactional(rollbackFor = Exception.class)
    default User createNewUser(String login, String password) {
        return save(new User(login, password));
    }

    default User updateRating(User user, double difference) {
        user.setRating(user.getRating() + difference);
        return save(user);
    }

    User findByLogin(String login);

    @Query("from User u where u.login like concat('%', :input, '%') and u.rating >= :start and u.rating <= :end " +
            "and u <> :user order by u.rating desc")
    Page<User> findOpponentsByRatingAndLoginInput
            (@Param("user") User user, @Param("input") String input,
             @Param("start") double start, @Param("end") double end, Pageable pageable);

}

