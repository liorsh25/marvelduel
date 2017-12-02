package com.lior.duel.game;

import com.lior.duel.dao.DuelsRepository;
import com.lior.duel.dao.HeroesRepository;
import com.lior.duel.model.Duel;
import com.lior.duel.model.Game;
import com.lior.duel.model.Hero;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Log
public class GameService {

    @Autowired
    private HeroesRepository heroesRepository;

    @Autowired
    private DuelsRepository duelsRepository;

    @Autowired
    private HeroesRandomGenerator heroesRandomGenerator;

    @Value("${duel.numOfHeroes}")
    protected int duelSize;
    @Value("${game.numOfDuels}")
    private int gameSize;

    @Value("${leadBoard.numofHeroes}")
    private int leadBoardSize;

    private Object voteLock = new Object();

    //@PostConstruct
    public void populateDB(){
        log.info("START: populateDB");
        try {

            InputStream is = getClass().getResourceAsStream("/marvel.csv");
            Reader targetReader = new InputStreamReader(is);

            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(targetReader);
            Hero hero;
            for (CSVRecord record : records) {
                addToDbIfNecessary(record);
            }

            log.info("END: populateDB");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addToDbIfNecessary(CSVRecord record) {
        log.info("addToDbIfNecessary");
        String name = record.get("name");

        //Add hero only if not in DB
        if(heroesRepository.findOne(name) == null){
            String thumbnailPath = record.get("thumbnail.path");
            String thumbnailExtension = record.get("thumbnail.extension");
            String imageUrl = thumbnailPath +"."+thumbnailExtension;

            Hero hero = new Hero(name, imageUrl);

            heroesRepository.save(hero);
        }


    }

    public Game generateNewGame(){
        Game retGame = null;
        List<Duel> duelsList = new ArrayList<Duel>();

        //generate 5 random duels from random hero
        int numberOfHeroesInGame = gameSize * duelSize;
        final List<Hero> heroes = heroesRandomGenerator.generateRandomHeroes(numberOfHeroesInGame);

        //now create the duels in the game
        for (int i = 0; i < numberOfHeroesInGame; i=i+duelSize) {
            List<Hero> currentHeroes = heroes.subList(i, i + duelSize);
            Duel duel = new Duel(currentHeroes);
            duelsList.add(duel);
            //TODO: add cleanup OLD duels
            duelsRepository.save(duel);
        }

        retGame = new Game(duelsList);
        return retGame;
    }

    public List<Hero> extractLeadHeroes(){
        Sort sortData = new Sort(Sort.Direction.DESC, "votes");//sort by number of votes
        Pageable pagingData = new PageRequest(0, leadBoardSize,sortData);//will take only the first leadBoardSize
        Page<Hero> heroesPage = heroesRepository.findAll(pagingData);
        return heroesPage.getContent();
    }

    public Hero findOneHero(String heroId){
        return heroesRepository.findOne(heroId);
    }
    public void voteForHero(String duelId,String heroId) throws Exception {
        log.info("Going to vote:"+ duelId +","+ heroId);
        final List<Duel> duelList = duelsRepository.findByDuelIdAndHeroId(duelId, heroId);

        //"fraud" protection check the duelId+heroId is valid
        validateDuel(duelList);
        //edit/duplicate vote protection
        protectFromDualVote(duelList);

        doVote(duelId,heroId);

    }

    private void doVote(String duelId, String heroId) {
        synchronized (voteLock) {//protect from several threads try to increment in the same time
            //update DB with vote
            updateDuelAsVoted(duelId);
            incVoteCount(heroId);
        }
    }

    private void protectFromDualVote(List<Duel> duelList) throws Exception {
        Duel duel = duelList.get(0);
        if(duel.isVoted()){
            //TODO: Make custom exceptions
            throw new Exception("duelId "+duel.getDuelId()+" already been voted");
        }
    }

    private void validateDuel(List<Duel> duelList) throws Exception {
        if(duelList==null || duelList.isEmpty()){
            //TODO: Make custom exceptions
            throw new Exception("duelId was not found");
        }
    }


    private void updateDuelAsVoted(String duelId) {
            Duel duel= duelsRepository.findOne(duelId);
            duel.setVoted(true);
            duelsRepository.save(duel);
            log.info("Mark as voted for dualId:"+duelId);
    }

    private void incVoteCount(String heroId) {
            Hero hero = heroesRepository.findOne(heroId);
            hero.setVotes(hero.getVotes() + 1);
            heroesRepository.save(hero);
            log.info("Increment vote for hero '"+hero.getName()+"'(heroId="+heroId+") to be "+ hero.getVotes());
    }
}
