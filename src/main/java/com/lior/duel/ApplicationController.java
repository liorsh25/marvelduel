package com.lior.duel;


import com.lior.duel.game.GameService;
import com.lior.duel.model.Game;
import com.lior.duel.model.Hero;
import lombok.extern.java.Log;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ApplicationController {
    private static Logger log = LogManager.getLogger(ApplicationController.class.getName());

    @Autowired
    GameService gameService;

    /**
     * I could give here a parameter to determine the size of one game (currently from configuration)
     *
     * @return
     */
    @RequestMapping("duel/getGame")
    public Game getGame() {
        log.info("START request: getGame");
        Game retGame = gameService.generateNewGame();
        return retGame;
    }


    @RequestMapping("duel/getLeadBoard")
    public List<Hero> getLeadBoard() {
        log.info("START request: getLeadBoard");
        List<Hero> retList = gameService.extractLeadHeroes();
        return retList;
    }

    @RequestMapping("duel/vote")
    //TODO: optional return JSON with error code to be used by the front end
    public boolean oneVote(@RequestParam("duelId") String duelId, @RequestParam("heroId") String heroId) {
        log.info("START request: vote");

        boolean isSucVote = false;
        try {
            gameService.voteForHero(duelId, heroId);
            isSucVote = true;
        } catch (Exception e) {
            log.error("Vote had failed.", e);
        }

        return isSucVote;
    }

}
