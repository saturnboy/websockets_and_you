package com.saturnboy.feed.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.saturnboy.feed.Game;
import com.saturnboy.feed.GameSocket;
import com.saturnboy.feed.Player;

public class PlayerTest {

	@Test
	public void testId() {
		Game game = new Game();
		GameSocket socket = new GameSocket(game);
		Player player = new Player(socket);

		assertThat(player.getId()).isEqualTo(socket.hashCode());
		assertThat(player.toString()).isEqualTo("Player " + socket.hashCode() + ": <unknown> 0");
	}
}
