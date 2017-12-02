package com.lior.duel.dao;

import com.lior.duel.model.Hero;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface HeroesRepository extends MongoRepository<Hero, String> {
}