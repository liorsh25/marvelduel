package com.lior.duel;

import com.lior.duel.model.Duel;
import com.lior.duel.model.Game;
import com.lior.duel.model.Hero;
import lombok.extern.java.Log;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Log
public class DuelApplicationTests {

	@Autowired
	private ApplicationController controller;

	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

	@Test
	public void testSimpleGameFlow() {
		final Game game = controller.getGame();
		final List<Duel> duels = game.getDuels();
		Map<String,Long> countersBeforeVotes = new HashMap<>();

		log.info("Go over duels in game and select heroes, for game "+ game.getGameId());
		//save the before counters
		countersBeforeVotes = getCountersFromBefore(duels);

		performVoteForAllDuels(duels);

		log.info("Check correct vote values, for game "+ game.getGameId());
		checkCorrectVoteCounters(duels,countersBeforeVotes,1);

		//get lead board
		log.info("Get lead board after game "+ game.getGameId());

		final List<Hero> leadBoard = controller.getLeadBoard();
		assertThat(leadBoard).isNotNull();
		assertThat(leadBoard.size()).isEqualTo(10);//TODO: take it from service configuration
		//TODO: test vote value correction
	}

	private void checkCorrectVoteCounters(List<Duel> duels, Map<String, Long> countersBeforeVotes,int incrementToTest) {

		//check correct vote values
		for (Duel duel : duels) {
			String firstHeroId = duel.getHeroes().get(0).getHeroId();
			long votesBefore = countersBeforeVotes.get(firstHeroId);
			long votesNow = controller.gameService.findOneHero(firstHeroId).getVotes();
			log.info("Check heroId "+firstHeroId+": voteNow="+votesNow+",votesBefore= "+ votesBefore+", incrementToTest="+ incrementToTest);

			assertThat(votesNow).isEqualTo(votesBefore+incrementToTest);
		}
	}

	private void performVoteForAllDuels(List<Duel> duels) {
		//do the votes
		for (Duel duel : duels) {
			String firstHeroId = duel.getHeroes().get(0).getHeroId();
			boolean ans = controller.oneVote(duel.getDuelId(), firstHeroId);
			assertThat(ans).isTrue();
		}
	}

	private Map<String, Long> getCountersFromBefore(List<Duel> duels){
		Map<String, Long> retMap = new HashMap<>();
		for (Duel duel : duels) {
			String firstHeroId = duel.getHeroes().get(0).getHeroId();
			final Hero oneHero = controller.gameService.findOneHero(firstHeroId);
			if(oneHero!=null) {
				retMap.put(firstHeroId, oneHero.getVotes());
			}
		}
		return retMap;
	}

	@Test
	public void testGameRandomness() {
		Game game1 = controller.getGame();
		Game game2 = controller.getGame();

		//check different heroes inside each game
		checkDifferenceInItems(game1);
		checkDifferenceInItems(game2);
		//check that the list of heroes in games is no equal list
		checkDifferenceBetweenGames(game1,game2);

	}

	private void checkDifferenceInItems(Game game) {
		//TODO: implement this
	}

	private void checkDifferenceBetweenGames(Game game1, Game game2) {
		//TODO: implement this
	}

	@Test
	public void testFraud(){
		boolean ans = controller.oneVote("fake-duelId","fake-heroId");
		assertThat(ans).isFalse();
	}

	@Test
	public void testProtectDuplicateVote(){
		final Game game = controller.getGame();
		final List<Duel> duels = game.getDuels();

		//do the votes
		for (Duel duel : duels) {
			String firstHeroId = duel.getHeroes().get(0).getHeroId();
			boolean ans = controller.oneVote(duel.getDuelId(), firstHeroId);
			assertThat(ans).isTrue();
			//fail the second vote
			ans = controller.oneVote(duel.getDuelId(), firstHeroId);
			assertThat(ans).isFalse();

		}
	}

	@Ignore
	@Test
	public void testMultiThread() {
		int numberOfGamesToTest = 50;
		List<Game> listOfGames = new ArrayList<>();
		Map<String,Map<String,Long>> beforeVoteCounters = new HashMap<>();

		listOfGames = controller.gameService.generateSameHeroesInGames(numberOfGamesToTest);

		//generate the games and save the before counters
		for (Game game: listOfGames) {
			Map<String,Long> beforeCounters = getCountersFromBefore(game.getDuels());
			beforeVoteCounters.put(game.getGameId(),beforeCounters);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(numberOfGamesToTest);

		//vote for the first hero in each duel
		for (Game game : listOfGames) {
			executorService.execute(() -> {
				performVoteForAllDuels(game.getDuels());
			});
		}

		//wait for all votes to be finished
		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//check the vote counter for each game
		for (Game game : listOfGames) {
			//Each hero vote counter should increment in numberOfGamesToTest
			checkCorrectVoteCounters(game.getDuels(),beforeVoteCounters.get(game.getGameId()),numberOfGamesToTest);
		}

	}


}
