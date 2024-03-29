package com.tonyp.onlinechess.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tonyp.onlinechess.tools.GameUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static com.tonyp.onlinechess.web.AppJpaConfiguration.JSON_DATE_FORMAT;

@Entity
@Table(name = "games")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private int id;

    @ManyToOne(optional = false)
    private User white;

    @ManyToOne(optional = false)
    private User black;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_to_move_id")
    private User playerToMove;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String fen;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(length = 1000)
    private String description;

    @Column(name = "legal_moves", nullable = false)
    private String legalMoves;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private UUID uuid;

    @Column(name = "last_modified_timestamp", nullable = false)
    @JsonFormat(pattern=JSON_DATE_FORMAT)
    private LocalDateTime lastModifiedTimestamp;

    @JsonIgnore
    @OneToMany(mappedBy = "game")
    private List<Move> moves;

    public Game(User white, User black) {
        this.white = white;
        this.black = black;
        playerToMove = white;
        fen = GameUtil.STARTING_POSITION_FEN;
        legalMoves = GameUtil.STARTING_POSITION_LEGAL_MOVES;
        isCompleted = false;
        lastModifiedTimestamp = Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime();
        uuid = UUID.randomUUID();
    }
}

