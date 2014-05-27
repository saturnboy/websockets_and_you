package com.saturnboy.chat;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * Simple embedded Jetty server with two endpoints:
 * <ol>
 * <li>{@code /chat} - a WebSocket endpoint</li>
 * <li>{@code /} - a Resource endpoint (serves html for the webapp client)</li>
 * </ol>
 */
public class ChatServer {
	private static final int PORT = 7006;

	public static void main(String[] args) {
		System.out.println("ChatServer: port=" + PORT);

		ContextHandlerCollection contexts = new ContextHandlerCollection();

		// chat handler @ /chat
		ContextHandler ctx = new ContextHandler("/chat");
		ctx.setHandler(new ChatHandler());
		contexts.addHandler(ctx);

		// resource handler @ / -- serves our html page for the web client
		ctx = new ContextHandler("/");
		ResourceHandler web = new ResourceHandler();
		web.setResourceBase(ChatServer.class.getClassLoader().getResource("web").toExternalForm());
		web.setDirectoriesListed(true);
		web.setWelcomeFiles(new String[] { "index.html" });
		ctx.setHandler(web);
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
