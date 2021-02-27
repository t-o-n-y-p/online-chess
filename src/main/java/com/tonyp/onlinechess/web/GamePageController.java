package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class GamePageController {

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @GetMapping("/game")
    public String main(Model model, @RequestParam int id) {
        System.out.println(id);
        User user = usersDao.findByLogin(OnlineChessApplication.USER_LOGIN);
        Game game = manager.find(Game.class, id);
        model.addAttribute("user", user);
        model.addAttribute("game", game);
        model.addAttribute("board", GameUtil.getBoard(game.getFen(), game.getWhite().equals(user) ? Color.WHITE : Color.BLACK));
        List<String> squares = IntStream.rangeClosed('a', 'h')
                .mapToObj(file -> IntStream.rangeClosed('1', '8').mapToObj(rank -> new String(new char[]{(char) file, (char) rank})))
                .flatMap(e -> e).collect(Collectors.toList());
        model.addAttribute("squares", squares);
        return "game";
    }

    @PostMapping("/game")
    public String makeMove(Model model, @RequestParam int id,
                           @RequestParam String square1,
                           @RequestParam String square2,
                           @RequestParam String promotion) {
        System.out.println(id);
        System.out.println(square1 + square2 + promotion);
        return main(model, id);
    }

}
