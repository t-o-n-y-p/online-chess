package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.model.MoveRestView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@Slf4j
public class MovesRestController {

    private final MovesRepository movesRepository;

    @GetMapping("/api/move/{moveId}")
    public ResponseEntity<MoveRestView> getMove(@PathVariable int moveId) {
        try {
            MoveRestView found = movesRepository.findByIdEquals(moveId, MoveRestView.class);
            return found == null ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                    : new ResponseEntity<>(found, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Move REST error", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred. Please try again later."
            );
        }
    }

}
