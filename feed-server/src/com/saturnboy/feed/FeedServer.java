package com.saturnboy.feed;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * Simple embedded Jetty server with one endpoints:
 * <ol>
 * <li>{@code /feed} - a WebSocket endpoint</li>
 * </ol>
 */
public class FeedServer {
	private static final int PORT = 7007;

	public static void main(String[] args) {
		System.out.println("FeedServer: port=" + PORT);

		ContextHandlerCollection contexts = new ContextHandlerCollection();

		// feed handler @ /feed
		ContextHandler ctx = new ContextHandler("/feed");
		ctx.setHandler(new FeedHandler());
		contexts.addHandler(ctx);

		Server server = new Server(PORT);
		server.setHandler(contexts);

		try {
			server.start();
			server.join();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
