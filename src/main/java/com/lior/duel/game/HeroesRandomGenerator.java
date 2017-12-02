package com.lior.duel.game;

import com.lior.duel.dao.HeroesRepository;
import com.lior.duel.dao.SampleOperation;
import com.lior.duel.model.Hero;
import com.mongodb.DBCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class HeroesRandomGenerator {

    @Autowired
    private HeroesRepository heroesRepository;

    @Autowired
    private MongoTemplate mongoTemplate;


    /***
     * Will generate Random different numberOfHeroes Heroes list
     * @param numberOfHeroes
     * @return
     */
    public List<Hero> generateRandomHeroes(int numberOfHeroes){
        List<Hero> retList;
        retList = randomHeroesFromDB(numberOfHeroes);
        //retList = slowRandomHeroes(numberOfHeroes);
        return retList;
    }

    /**
     * Using MongoDB "sample" ability to get random heroes
     * @param numberOfHeroes
     * @return
     */
    private List<Hero> randomHeroesFromDB(int numberOfHeroes) {
        List<Hero> retList;
        SampleOperation sampleOperation = new SampleOperation(numberOfHeroes);
        TypedAggregation<Hero> typedAggr = Aggregation.newAggregation(Hero.class,sampleOperation);
        AggregationResults<Hero> aggregationResults = mongoTemplate.aggregate(typedAggr, Hero.class);
        retList = aggregationResults.getMappedResults();
        return retList;
    }

    @Deprecated
    private List<Hero> slowRandomHeroes(int numberOfHeroes){
        //take the hero list from DB
        //TODO: improve this (take the list from DB only once). Maybe to hold a "cache" of future games when application start (and refill it async)
        List<Hero> heroesListFromDB = heroesRepository.findAll();
        List<Hero> retList = new ArrayList<Hero>();

        Random rand = new Random();
        for (int i = 0; i < numberOfHeroes; i++) {
            int randomIndex = rand.nextInt(heroesListFromDB.size());
            Hero randomElement = heroesListFromDB.get(randomIndex);
            retList.add(randomElement);
            heroesListFromDB.remove(randomIndex);
        }

        return retList;

    }

}
