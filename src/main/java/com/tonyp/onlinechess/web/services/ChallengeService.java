package com.tonyp.onlinechess.web.services;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ChallengeService {

    private final ChallengesRepository challengesRepository;
    private final GamesRepository gamesRepository;

    @Transactional(rollbackFor = Exception.class)
    public void acceptChallenge(Challenge acceptedChallenge) {
        challengesRepository.delete(acceptedChallenge);
        if (acceptedChallenge.getTargetColor().equals(Color.WHITE)) {
            gamesRepository.createNewGame(acceptedChallenge.getTo(), acceptedChallenge.getFrom());
        } else {
            gamesRepository.createNewGame(acceptedChallenge.getFrom(), acceptedChallenge.getTo());
        }
    }

}
