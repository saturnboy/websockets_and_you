package com.saturnboy.feed.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;

import com.saturnboy.feed.Game;
import com.saturnboy.feed.GameSocket;

public class GameTest {

	@Test
	public void testGetPoint() throws Exception {
		Game game = new Game();
		Method method = Game.class.getDeclaredMethod("getPoint", Double.TYPE, Double.TYPE);
		method.setAccessible(true);

		String out = (String) method.invoke(game, 1.234567d, 7.654321d);
		assertThat(out).isEqualTo("1.2346 7.6543");

		out = (String) method.invoke(game, 1.11115d, 1.22225d);
		assertThat(out).isEqualTo("1.1112 1.2223");
	}

	@Test
	public void testRandomPos() throws Exception {
		Game game = new Game();
		Method method = Game.class.getDeclaredMethod("getRandomPos");
		method.setAccessible(true);

		String out = (String) method.invoke(game);
		assertThat(out).matches("0\\.\\d{4} 0\\.\\d{4}");
	}

	@Test
	public void testRandomVel() throws Exception {
		Game game = new Game();
		Method method = Game.class.getDeclaredMethod("getRandomVel", Double.TYPE);
		method.setAccessible(true);

		String out = (String) method.invoke(game, 1.0d);
		assertThat(out).matches("-?0\\.\\d{4} -?0\\.\\d{4}");
	}

	@Test
	public void testOnJoin() {
		RemoteEndpoint mockRemote = mock(RemoteEndpoint.class);

		Session mockSession = mock(Session.class);
		when(mockSession.getRemote()).thenReturn(mockRemote);

		GameSocket mockSocket = mock(GameSocket.class);
		when(mockSocket.getSession()).thenReturn(mockSession);

		String pat = "WELCOME\\|" + mockSocket.hashCode() + " 0\\.\\d{4} 0\\.\\d{4}";
		when(mockRemote.sendStringByFuture(matches(pat))).thenReturn(null);

		Game game = new Game();
		game.onJoin(mockSocket);

		verify(mockRemote).sendStringByFuture(matches(pat));
		verifyNoMoreInteractions(mockRemote);

		verify(mockSession).getRemote();
		verifyNoMoreInteractions(mockSession);

		verify(mockSocket).getSession();
		verifyNoMoreInteractions(mockSocket);
	}
}
