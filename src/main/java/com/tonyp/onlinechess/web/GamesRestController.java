package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.model.LastMoveRestView;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class GamesRestController {

    private final MovesRepository movesRepository;

    @GetMapping("/api/game/{gameId}/lastMove")
    public ResponseEntity<LastMoveRestView> getLastMove(@PathVariable int gameId) {
        try {
            LastMoveRestView found = movesRepository.findFirstByGame_IdOrderByIdDesc(gameId, LastMoveRestView.class);
            return found == null ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                    : new ResponseEntity<>(found, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred. Please try again later."
            );
        }
    }

}
