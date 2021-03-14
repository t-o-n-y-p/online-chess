package com.tonyp.onlinechess.model;

import com.tonyp.onlinechess.tools.GameUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Objects;

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

    @ManyToOne(optional = false)
    @EqualsAndHashCode.Include
    private Game game;

    @Column(nullable = false, length = 5)
    @Pattern(regexp = "([a-h][1-8]){2}[qrbn]?")
    private String value;

    @Column(name = "repetition_info", nullable = false, length = 100)
    @NotBlank
    private String repetitionInfo;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String fen;

    public Move(Game game, String value, String fen) {
        this.game = game;
        this.value = value;
        this.fen = fen;
        repetitionInfo = GameUtil.getPositionFromFen(game.getFen());
    }

}

