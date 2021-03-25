package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ChallengesRepository extends JpaRepository<Challenge, Integer> {

    @Transactional(rollbackFor = Exception.class)
    default Challenge createNewChallenge(User from, User to, Color targetColor) {
        return save(new Challenge(from, to, targetColor));
    }

    Page<Challenge> findByToOrderByTimestampDesc(User user, Pageable pageable);

    Page<Challenge> findByToAndFrom_LoginContainingOrderByTimestampDesc(User user, String input, Pageable pageable);

}

