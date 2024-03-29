package com.tonyp.onlinechess.model;

import java.util.UUID;

public interface MoveRestView {

    int getId();
    IdOnlyRestView getPreviousMove();
    IdOnlyRestView getNextMove();
    String getNotation();
    String getRepetitionInfo();
    String getBoard();
    UUID getUuid();

}
