package com.tonyp.onlinechess.model;

import java.util.UUID;

public interface MoveView {

    int getId();
    Game getGame();
    PreviousNextMoveView getPreviousMove();
    PreviousNextMoveView getNextMove();
    String getValue();
    String getRepetitionInfo();
    String getFen();
    UUID getUuid();

}
