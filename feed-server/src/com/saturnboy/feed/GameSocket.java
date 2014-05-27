package com.saturnboy.feed;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Simple Jetty WebSocket POJO with annotated handlers for each type of message (connect, close,
 * error, message). Since each client gets a new instance of this class, we instantiate with the
 * same instance of {@link Game} to create a single shared world.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class GameSocket {
	private Game game;
	private Session session;

	public GameSocket(Game game) {
		this.game = game;
	}

	public Session getSession() {
		return session;
	}

	/** New connection, so join shared chat. */
	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.println("CONNECT " + session.hashCode());
		this.session = session;
		game.onJoin(this);
	}

	/** Close connection, so leave shared chat. */
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.println("CLOSE " + session.hashCode() + ": " + statusCode + " "
				+ (reason == null ? "unknown" : reason));
		session.close();
		game.onLeave(this.hashCode());
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		System.out.println("ERROR " + session.hashCode() + ": " + t.getMessage());
	}

	/**
	 * Handler for incoming text messages (no binary message handling).
	 * 
	 * @param message
	 *            the incoming text message
	 * @throws InterruptedException
	 */
	@OnWebSocketMessage
	public void onText(String message) throws InterruptedException {
		System.out.println("MESSAGE " + session.hashCode() + ": " + message);
		if (message.startsWith("NAME|")) {
			game.onName(hashCode(), message.substring(5));
		} else if (message.startsWith("POS|")) {
			game.onPos(hashCode(), message.substring(4));
		} else if (message.startsWith("EAT|")) {
			game.onEatFood(hashCode(), Integer.parseInt(message.substring(4)));
		}
	}
}