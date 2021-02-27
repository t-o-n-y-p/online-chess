package com.tonyp.onlinechess.model;

import com.tonyp.onlinechess.tools.GameUtil;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(optional = false)
    private User white;

    @ManyToOne(optional = false)
    private User black;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_to_move_id")
    private User playerToMove;

    @Column(nullable = false, length = 100)
    private String fen;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(length = 1000)
    private String description;

    @Column(name = "legal_moves", nullable = false)
    private String legalMoves;

    @Column(name = "last_modified_timestamp", nullable = false)
    private LocalDateTime lastModifiedTimestamp;

    @OneToMany(mappedBy = "game")
    private List<Move> moves;

    public Game() {
    }

    public Game(User white, User black) {
        this.white = white;
        this.black = black;
        playerToMove = white;
        fen = GameUtil.STARTING_POSITION_FEN;
        legalMoves = GameUtil.STARTING_POSITION_LEGAL_MOVES;
        isCompleted = false;
        lastModifiedTimestamp = Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return id == game.id && isCompleted == game.isCompleted && white.equals(game.white) && black.equals(game.black)
                && playerToMove.equals(game.playerToMove) && fen.equals(game.fen) && description.equals(game.description)
                && legalMoves.equals(game.legalMoves) && lastModifiedTimestamp.equals(game.lastModifiedTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, white, black, playerToMove, fen, isCompleted, description, legalMoves, lastModifiedTimestamp);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getWhite() {
        return white;
    }

    public void setWhite(User white) {
        this.white = white;
    }

    public User getBlack() {
        return black;
    }

    public void setBlack(User black) {
        this.black = black;
    }

    public User getPlayerToMove() {
        return playerToMove;
    }

    public void setPlayerToMove(User playerToMove) {
        this.playerToMove = playerToMove;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLegalMoves() {
        return legalMoves;
    }

    public void setLegalMoves(String legalMoves) {
        this.legalMoves = legalMoves;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public List<String> getPositionHistory() {
        if (moves == null) {
            return new ArrayList<>();
        }
        return moves.stream()
                .map(Move::getRepetitionInfo)
                .collect(Collectors.toList());
    }
}

