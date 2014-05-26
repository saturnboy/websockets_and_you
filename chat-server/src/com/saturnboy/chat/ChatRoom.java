package com.saturnboy.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Singleton chat room to create a shared chat for all connected clients. Maintains a small
 * in-memory queue of the latest messages which is sent to new members to catch them up on the
 * conversation.
 */
public enum ChatRoom {
	/** Singleton */
	INSTANCE;

	private static final int FIXED_SIZE = 8;

	/** Synchronized queue of messages with fixed size. */
	private BlockingQueue<String> messages = new LinkedBlockingQueue<String>(FIXED_SIZE);

	/** Synchronized list of members. */
	private List<ChatSocket> members = Collections.synchronizedList(new ArrayList<ChatSocket>());

	/** New member joining, so add to synchronized members list. */
	public void join(ChatSocket member) {
		members.add(member);

		// new member joined, so send any queued messages
		for (String message : messages) {
			member.getSession().getRemote().sendStringByFuture(message);
		}
	}

	/** Member leaving, so remove from synchronized members list. */
	public void leave(ChatSocket member) {
		members.remove(member);
	}

	/** Send async message to ALL members. */
	public synchronized void send(String message) throws InterruptedException {
		// add message to queue (removing old messages if queue too big)
		while (messages.size() >= FIXED_SIZE) {
			messages.take();
		}
		messages.put(message);

		// send message to ALL members
		for (ChatSocket member : members) {
			member.getSession().getRemote().sendStringByFuture(message);
		}
	}
}
