package com.saturnboy.feed;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

/**
 * Each new connection invokes {@code createWebSocket()} to first instantiate WebSocket-annotated
 * class and then return it.
 */
public class GameSocketCreator implements WebSocketCreator {
	private Game game;

	public GameSocketCreator(Game game) {
		this.game = game;
	}

	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		return new GameSocket(game);
	}
}