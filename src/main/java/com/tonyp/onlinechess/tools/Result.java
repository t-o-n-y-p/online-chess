package com.tonyp.onlinechess.tools;

import com.tonyp.onlinechess.model.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Result {

    UNDEFINED("Play continues", null),
    WHITE_WON_BY_CHECKMATE("White won by checkmate", Color.WHITE),
    WHITE_WON_BY_RESIGNATION("White won by resignation", Color.WHITE),
    BLACK_WON_BY_CHECKMATE("Black won by checkmate", Color.BLACK),
    BLACK_WON_BY_RESIGNATION("Black won by resignation", Color.BLACK),
    DRAW_BY_FIFTY_MOVE_RULE("Draw by fifty move rule", null),
    DRAW_BY_STALEMATE("Draw by stalemate", null),
    DRAW_BY_INSUFFICIENT_MATERIAL("Draw by insufficient material", null),
    DRAW_BY_REPETITION("Draw by repetition", null);

    private final String description;
    private final Color winningSide;

}

