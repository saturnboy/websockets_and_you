package com.saturnboy.feed;

import java.util.concurrent.Future;

public class Player {
	private GameSocket socket;
	private String name;
	private int score;
	private String pos;

	public Player(GameSocket socket) {
		this.socket = socket;
		name = "<unknown>";
		score = 0;
	}

	public int getId() {
		return socket.hashCode();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getScore() {
		return score;
	}

	public int incrScore() {
		score++;
		return score;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public Future<Void> send(String message) {
		return socket.getSession().getRemote().sendStringByFuture(message);
	}

	@Override
	public String toString() {
		return "Player " + getId() + ": " + name + " " + score;
	}
}
