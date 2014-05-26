package com.saturnboy.chat;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Simple embedded Jetty server with two endpoints:
 * <ol>
 * <li>{@code /chat} - a WebSocket endpoint</li>
 * <li>{@code /} - a Resource endpoint (serves html for the webapp client)</li>
 * </ol>
 */
public class ChatServer {
	private static final int PORT = 7117;

	public static void main(String[] args) {
		System.out.println("ChatServer");

		ContextHandlerCollection contexts = new ContextHandlerCollection();

		// chat handler @ /chat
		ContextHandler ctx = new ContextHandler("/chat");
		ctx.setHandler(new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				// register the ChatSocket class -- every ws connection returns a new instance
				factory.register(ChatSocket.class);
				// set idle time to 15min before socket is closed
				factory.getPolicy().setIdleTimeout(900000);
			}
		});
		contexts.addHandler(ctx);

		// resource handler @ / -- serves our html for the web client (right out of the jar)
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
			System.out.println("  url=http://localhost:" + PORT + "/");
			System.out.println("  chat=ws://localhost:" + PORT + "/chat/");
			server.start();
			server.join();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("server failed to start");
		}
	}
}
