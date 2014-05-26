package com.saturnboy.chat;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Simple Jetty WebSocket POJO with annotated handlers for each type of message (connect, close,
 * error, message). Since each client gets a new instance of this class, use a singleton
 * {@link ChatRoom} to create a single shared chat. Only handles plain text messages, binary
 * messages not supported.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class ChatSocket {
	private Session session;

	public Session getSession() {
		return session;
	}

	/** New connection, so join shared chat. */
	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.println("CONNECT " + session.hashCode());
		this.session = session;
		ChatRoom.INSTANCE.join(this);
	}

	/** Close connection, so leave shared chat. */
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.println("CLOSE " + session.hashCode() + ": " + statusCode + " "
				+ (reason == null ? "unknown" : reason));
		session.close();
		ChatRoom.INSTANCE.leave(this);
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
		ChatRoom.INSTANCE.send(message);
	}
}