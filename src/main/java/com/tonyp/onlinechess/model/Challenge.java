package com.tonyp.onlinechess.model;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    public Challenge() {
    }

    public Challenge(User from, User to, Color targetColor) {
        this.from = from;
        this.to = to;
        this.targetColor = targetColor;
        timestamp = Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime();
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
}

