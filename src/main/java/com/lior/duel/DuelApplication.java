package com.lior.duel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.lior.duel"})
public class DuelApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuelApplication.class, args);
	}

}
