package com.tonyp.onlinechess.model;

import java.util.UUID;

public interface MoveRestView {

    int getId();
    Game getGame();
    IdOnlyRestView getPreviousMove();
    IdOnlyRestView getNextMove();
    String getValue();
    String getRepetitionInfo();
    String getFen();
    UUID getUuid();

}
