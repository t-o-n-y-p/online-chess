package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;

@Controller
@SessionAttributes("user-session")
public class ChallengeController {

    private EntityManager manager;
    private UsersDao usersDao;
    private ChallengesDao challengesDao;
    private GamesDao gamesDao;

    public ChallengeController(EntityManager manager, UsersDao usersDao, ChallengesDao challengesDao, GamesDao gamesDao) {
        this.manager = manager;
        this.usersDao = usersDao;
        this.challengesDao = challengesDao;
        this.gamesDao = gamesDao;
    }

    @PostMapping("/challenge/accept")
    public RedirectView accept(RedirectAttributes attributes,
                               @RequestParam int id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "false", name = "to_previous_page") boolean toPreviousPage,
                               @RequestParam(defaultValue = "false", name = "from_challenges") boolean fromChallenges,
                               @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("/login");
        }
        try {
            Challenge acceptedChallenge = manager.find(Challenge.class, id);
            manager.getTransaction().begin();
            manager.remove(acceptedChallenge);
            if (acceptedChallenge.getTargetColor().equals(Color.WHITE)) {
                gamesDao.createNewGame(acceptedChallenge.getTo(), acceptedChallenge.getFrom());
            } else {
                gamesDao.createNewGame(acceptedChallenge.getFrom(), acceptedChallenge.getTo());
            }
            manager.getTransaction().commit();

            attributes.addAttribute("challenge_accepted", true);
            return getAcceptChallengeRedirectView(attributes, page, toPreviousPage, fromChallenges);
        } catch (Throwable e) {
            attributes.addAttribute("error", true);
            return getAcceptChallengeRedirectView(attributes, page, toPreviousPage, fromChallenges);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
        }
    }

    @PostMapping("/challenge")
    public RedirectView challenge(RedirectAttributes attributes,
                                  @RequestParam(name = "opponent_id") int opponentId,
                                  @RequestParam(name = "target_color") Color targetColor,
                                  @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("/login");
        }
        try {
            manager.getTransaction().begin();
            challengesDao.createNewChallenge(
                    usersDao.findByLogin(session.getLogin()),
                    manager.find(User.class, opponentId),
                    targetColor
            );
            manager.getTransaction().commit();

            attributes.addAttribute("challenge_created", true);
            return new RedirectView("/main");
        } catch (Throwable e) {
            attributes.addAttribute("error", true);
            return new RedirectView("/main");
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

    private RedirectView getAcceptChallengeRedirectView
            (RedirectAttributes attributes, int page, boolean toPreviousPage, boolean fromChallenges) {
        if (fromChallenges) {
            if (toPreviousPage) {
                attributes.addAttribute("page", page - 1);
            } else {
                attributes.addAttribute("page", page);
            }
            return new RedirectView("/challenges");
        } else {
            return new RedirectView("/main");
        }
    }
}
