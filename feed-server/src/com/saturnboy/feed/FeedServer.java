package com.saturnboy.feed;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Simple embedded Jetty server with one endpoints:
 * <ol>
 * <li>{@code /feed} - a WebSocket endpoint</li>
 * </ol>
 */
public class FeedServer {
	private static final int PORT = 7007;

	public static void main(String[] args) {
		System.out.println("FeedServer");

		final Game game = new Game();

		ContextHandlerCollection contexts = new ContextHandlerCollection();

		// feed handler @ /feed
		ContextHandler ctx = new ContextHandler("/feed");
		ctx.setHandler(new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				// register the creator -- every connection returns a new socket
				factory.setCreator(new GameSocketCreator(game));
				// set idle time to 15min before socket is closed
				factory.getPolicy().setIdleTimeout(900000);
			}
		});
		contexts.addHandler(ctx);

		Server server = new Server(PORT);
		server.setHandler(contexts);

		try {
			System.out.println("  url=http://localhost:" + PORT + "/");
			System.out.println("  feed=ws://localhost:" + PORT + "/feed/");
			server.start();
			server.join();
		} catch (Exception ex) {
			System.out.println("ERROR: feed server failed to start");
			ex.printStackTrace();
		}
	}
}
