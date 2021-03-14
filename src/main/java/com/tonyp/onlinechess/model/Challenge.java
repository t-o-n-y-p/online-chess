package com.tonyp.onlinechess.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "challenges")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Challenge {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private int id;

    @ManyToOne(optional = false)
    private User from;

    @ManyToOne(optional = false)
    private User to;

    @Column(name = "target_color", nullable = false)
    private Color targetColor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private UUID uuid;

    public Challenge(User from, User to, Color targetColor) {
        this.from = from;
        this.to = to;
        this.targetColor = targetColor;
        timestamp = Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime();
        uuid = UUID.randomUUID();
    }
}

