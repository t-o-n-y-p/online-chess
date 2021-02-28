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
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;

@Controller
public class GamePageController {

    private final EntityManager manager;
    private final UsersDao usersDao;

    public GamePageController(@Autowired EntityManager manager, @Autowired UsersDao usersDao) {
        this.manager = manager;
        this.usersDao = usersDao;
    }

    @GetMapping("/game")
    public String game(Model model, @RequestParam int id,
                       @RequestParam(defaultValue = "false", name = "legal_move") boolean legalMove,
                       @RequestParam(defaultValue = "false", name = "illegal_move") boolean illegalMove,
                       @RequestParam(defaultValue = "false") boolean error) {
        model.addAttribute("legalMove", legalMove);
        model.addAttribute("illegalMove", illegalMove);
        model.addAttribute("error", error);
        User user = usersDao.findByLogin(OnlineChessApplication.USER_LOGIN);
        Game game = manager.find(Game.class, id);
        model.addAttribute("user", user);
        model.addAttribute("game", game);
        model.addAttribute("board", GameUtil.getBoard(
                game.getFen(), game.getWhite().equals(user) ? Color.WHITE : Color.BLACK
        ));
        model.addAttribute("squares", GameUtil.SQUARES);
        return "game";
    }

}
