package com.lior.duel.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Document(collection = "heroes")
public class Hero {
    @Id
    private final String heroId = UUID.randomUUID().toString();
    private final String name;
    private final String imageUrl;
    private long votes;
}
