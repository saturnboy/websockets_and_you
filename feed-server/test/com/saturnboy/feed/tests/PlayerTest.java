package com.saturnboy.feed.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.saturnboy.feed.Game;
import com.saturnboy.feed.FeedSocket;
import com.saturnboy.feed.Player;

public class PlayerTest {

	@Test
	public void testId() {
		Game game = new Game();
		FeedSocket socket = new FeedSocket(game);
		Player p = new Player(socket);

		assertThat(p.getId()).isEqualTo(socket.hashCode());
		assertThat(p.toString()).isEqualTo("Player " + p.getId() + ": <name> 0 0,0");
	}

	@Test
	public void testName() {
		Game game = new Game();
		FeedSocket socket = new FeedSocket(game);
		Player p = new Player(socket);

		p.setName("Fred");
		assertThat(p.getName()).isEqualTo("Fred");
		assertThat(p.toString()).isEqualTo("Player " + p.getId() + ": Fred 0 0,0");
	}

	@Test
	public void testScore() {
		Game game = new Game();
		FeedSocket socket = new FeedSocket(game);
		Player p = new Player(socket);

		assertThat(p.getScore()).isEqualTo(0);
		p.incrScore();
		assertThat(p.getScore()).isEqualTo(1);
		p.incrScore();
		assertThat(p.getScore()).isEqualTo(2);
		assertThat(p.toString()).isEqualTo("Player " + p.getId() + ": <name> 2 0,0");
	}

	@Test
	public void testPos() {
		Game game = new Game();
		FeedSocket socket = new FeedSocket(game);
		Player p = new Player(socket);

		p.setPos("123 456");
		assertThat(p.getPos()).isEqualTo("123 456");
		assertThat(p.toString()).isEqualTo("Player " + p.getId() + ": <name> 0 123,456");
	}
}
