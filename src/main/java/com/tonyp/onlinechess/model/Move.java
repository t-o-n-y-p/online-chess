package com.tonyp.onlinechess.model;

import com.tonyp.onlinechess.tools.GameUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.UUID;

@Entity
@Table(name = "moves")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Move {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Game game;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_move_id")
    private Move previousMove;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_move_id")
    private Move nextMove;

    @Column(nullable = false, length = 20)
    private String notation;

    @Column(name = "repetition_info", nullable = false, length = 100)
    @NotBlank
    private String repetitionInfo;

    @Column(nullable = false, length = 64)
    @Pattern(regexp = "[\\spPrRnNbBqQkK]{64}")
    private String board;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private UUID uuid;

    public Move(Game game, Move lastMove, String value, String fen) {
        this.game = game;
        this.previousMove = lastMove;
        notation = GameUtil.getNotation(game.getFen(), value);
        board = GameUtil.getBoard(fen);
        repetitionInfo = GameUtil.getPositionFromFen(game.getFen());
        uuid = UUID.randomUUID();
    }
}

