package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
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
import java.util.List;

@Repository
public interface ChallengesRepository extends JpaRepository<Challenge, Integer> {

    @Transactional(rollbackFor = Exception.class)
    default Challenge createNewChallenge(User from, User to, Color targetColor) {
        return save(new Challenge(from, to, targetColor));
    }

    Page<Challenge> findByToOrderByTimestampDesc(User user, Pageable pageable);

    @Query("from Challenge c where c.to = :user and c.from.login like concat('%', :input, '%') order by c.timestamp desc")
    Page<Challenge> findIncomingChallengesByOpponentLoginInput
            (@Param("user") User user, @Param("input") String input, Pageable pageable);

}

