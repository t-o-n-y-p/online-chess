package com.tonyp.onlinechess.tools;

import xyz.niflheim.stockfish.StockfishClient;
import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Variant;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

import java.util.concurrent.ExecutionException;

public final class StockfishUtil {

    private StockfishUtil(){
    }

    private static final StockfishClient client;

    static {
        try {
            client = new StockfishClient.Builder()
                    .setInstances(4)
                    .setOption(Option.Threads, 4)
                    .setVariant(Variant.BMI2)
                    .build();
        } catch (StockfishInitException e) {
            throw new RuntimeException("StockfishInitException: " + e.getMessage());
        }
    }

    public static String getLegalMoves(String fen) throws ExecutionException, InterruptedException {
        return client.getLegalMoves(fen).get();
    }

    public static String getCheckers(String fen) throws ExecutionException, InterruptedException {
        return client.getCheckers(fen).get();
    }

    public static String makeMove(String fen, String move) throws ExecutionException, InterruptedException {
        return client.makeMove(fen, move).get();
    }

}

