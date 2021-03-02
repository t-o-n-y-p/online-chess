package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class OnlineChessApplication {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        StockfishUtil.makeMove(GameUtil.STARTING_POSITION_FEN, "e2e4");
        SpringApplication.run(OnlineChessApplication.class, args);
    }

}
