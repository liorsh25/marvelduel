package com.lior.duel.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "duels")
public class Duel {
    @Id
    private String duelId = UUID.randomUUID().toString();
    private final List<Hero> heroes;
    private boolean voted = false;
}
