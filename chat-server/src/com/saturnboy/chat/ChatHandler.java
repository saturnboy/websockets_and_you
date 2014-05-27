package com.saturnboy.chat;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class ChatHandler extends WebSocketHandler {

	@Override
	public void configure(WebSocketServletFactory factory) {
		// register the ChatSocket class -- every ws connection will return a new instance
		factory.register(ChatSocket.class);

		// set idle time to 15min before socket is closed
		factory.getPolicy().setIdleTimeout(15 * 60 * 1000L);
	}
}
