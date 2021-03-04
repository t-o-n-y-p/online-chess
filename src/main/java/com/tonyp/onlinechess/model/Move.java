package com.tonyp.onlinechess.model;

import com.tonyp.onlinechess.tools.GameUtil;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "moves")
public class Move {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(optional = false)
    private Game game;

    @Column(nullable = false, length = 5)
    private String value;

    @Column(name = "repetition_info", nullable = false, length = 100)
    private String repetitionInfo;

    public Move() {
    }

    public Move(Game game, String value) {
        this.game = game;
        this.value = value;
        repetitionInfo = GameUtil.getPositionFromFen(game.getFen());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return id == move.id && Objects.equals(game, move.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, game);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRepetitionInfo() {
        return repetitionInfo;
    }

    public void setRepetitionInfo(String repetitionInfo) {
        this.repetitionInfo = repetitionInfo;
    }
}

