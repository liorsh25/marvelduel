package com.lior.duel;

import com.lior.duel.model.Duel;
import com.lior.duel.model.Game;
import com.lior.duel.model.Hero;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
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
		Map<String,Long> heroMapForTest = new HashMap<>();

		//do the votes
		for (Duel duel : duels) {
			String firstHeroId = duel.getHeroes().get(0).getHeroId();
			saveHeroCountersBeforeVote(firstHeroId,heroMapForTest);
			boolean ans = controller.oneVote(duel.getDuelId(), firstHeroId);
			assertThat(ans).isTrue();
		}

		//check correct vote values
		for (Duel duel : duels) {
			String firstHeroId = duel.getHeroes().get(0).getHeroId();
			long votesBefore = heroMapForTest.get(firstHeroId);
			long votesNow = controller.gameService.findOneHero(firstHeroId).getVotes();
			assertThat(votesNow).isEqualTo(votesBefore+1);
		}

		//get lead board
		final List<Hero> leadBoard = controller.getLeadBoard();
		assertThat(leadBoard).isNotNull();
		assertThat(leadBoard.size()).isEqualTo(10);//TODO: take it from service configuration
		//TODO: test vote value correction
	}

	private void saveHeroCountersBeforeVote(String firstHeroId, Map<String, Long> heroMapForTest) {
		final Hero oneHero = controller.gameService.findOneHero(firstHeroId);
		if(oneHero!=null) {
			heroMapForTest.put(firstHeroId, oneHero.getVotes());
		}
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
	//TODO: make it work
	public void testMultiThread(){
		final Game game = controller.getGame();
		final List<Duel> duels = game.getDuels();

		ExecutorService executorService = Executors.newFixedThreadPool(10);

		executorService.execute(new Runnable() {
			public void run() {
				System.out.println("Asynchronous task");
			}
		});

		executorService.shutdown();

	}


}