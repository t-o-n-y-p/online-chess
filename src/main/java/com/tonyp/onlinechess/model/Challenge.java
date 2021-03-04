package com.tonyp.onlinechess.model;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(optional = false)
    private User from;

    @ManyToOne(optional = false)
    private User to;

    @Column(name = "target_color")
    private Color targetColor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(unique = true, nullable = false)
    private UUID uuid;

    public Challenge() {
    }

    public Challenge(User from, User to, Color targetColor) {
        this.from = from;
        this.to = to;
        this.targetColor = targetColor;
        timestamp = Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime();
        uuid = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Challenge challenge = (Challenge) o;
        return id == challenge.id && Objects.equals(uuid, challenge.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public Color getTargetColor() {
        return targetColor;
    }

    public void setTargetColor(Color targetColor) {
        this.targetColor = targetColor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}

