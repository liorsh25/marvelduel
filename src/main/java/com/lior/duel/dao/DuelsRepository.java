package com.lior.duel.dao;

import com.lior.duel.model.Duel;
import com.lior.duel.model.Hero;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DuelsRepository extends MongoRepository<Duel, String> {

    @Query("{'duelId': '?0'	, 'heroes.heroId': '?1'}")
    public List<Duel> findByDuelIdAndHeroId(String duelId, String heroId);
}
