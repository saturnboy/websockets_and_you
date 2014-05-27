package com.saturnboy.feed;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class FeedHandler extends WebSocketHandler {
	private final Game game;

	public FeedHandler() {
		this(new Game());
	}

	public FeedHandler(Game game) {
		this.game = game;
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		// register the creator -- every connection returns a new socket
		factory.setCreator(new GameSocketCreator());

		// set idle time to 15min before socket is closed
		factory.getPolicy().setIdleTimeout(15 * 60 * 1000L);
	}

	private class GameSocketCreator implements WebSocketCreator {

		@Override
		public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
			return new FeedSocket(game);
		}
	}
}
