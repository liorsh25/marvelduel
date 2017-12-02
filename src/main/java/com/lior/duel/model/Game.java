package com.lior.duel.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Data
public class Game {
    private String gameId = UUID.randomUUID().toString();
    private final List<Duel> duels;
}
