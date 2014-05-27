package com.saturnboy.feed;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Game {
	private static final int PLAYER_START_SIZE = 10;
	private static final int PLAYER_MIN = 2;
	private static final int SCORE_MAX = 5;
	private static final int FOOD_MAX = 10;
	private static final int FOOD_INTERVAL = 3;
	private static final double FOOD_VELOCITY_SCALE = 10.0d;

	private Map<Integer, Player> players;
	private SortedSet<Integer> foods;
	private int foodId;
	private boolean started;
	private ScheduledExecutorService executor;

	public Game() {
		players = Collections.synchronizedMap(new HashMap<Integer, Player>());
		foods = Collections.synchronizedSortedSet(new TreeSet<Integer>());
		foodId = 1;
		started = false;
		executor = Executors.newScheduledThreadPool(2);
	}

	/** New player joining. */
	public synchronized void onJoin(GameSocket socket) {
		Player player = new Player(socket);
		player.setPos(getRandomPos());
		players.put(player.getId(), player);

		String msg = player.getId() + " " + player.getPos();
		player.send("WELCOME|" + msg);
		sendAllExcept("OTHER|" + msg, player.getId());

		if (players.size() >= PLAYER_START_SIZE) {
			// start the game
			if (!started) {
				started = true;
				sendAll("START");

				executor.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						doSpawnFood();
					}
				}, 1, FOOD_INTERVAL, TimeUnit.SECONDS);
			}
		}
	}

	/** Player leaving. */
	public synchronized void onLeave(int playerId) {
		players.remove(playerId);

		if (players.size() < PLAYER_MIN) {
			// too few players, so stop the game...
			doEnd();
		}
	}

	public synchronized void onName(int playerId, String name) {
		Player player = getPlayer(playerId);
		player.setName(name);
	}

	public synchronized void onPos(int playerId, String pos) {
		Player player = getPlayer(playerId);
		player.setPos(pos);
		sendAllExcept("OTHER|" + player.getId() + " " + pos, player.getId());
	}

	public synchronized void onEatFood(int playerId, int foodId) {
		if (foods.contains(foodId)) {
			foods.remove(foodId);
			sendAll("YUM|" + foodId);

			Player player = getPlayer(playerId);
			int score = player.incrScore();

			if (score >= SCORE_MAX) {
				// max score reached, so player won!
				doEnd();
			}
		}
	}

	public synchronized void doEnd() {
		StringBuilder sb = new StringBuilder("END|");
		for (Player p : players.values()) {
			sb.append(p.getId()).append(":").append(p.getScore()).append(" ");
		}
		sendAll(sb.substring(0, sb.length() - 1));
	}

	public synchronized void doSpawnFood() {
		if (foods.size() > FOOD_MAX) {
			int firstId = foods.first();
			foods.remove(firstId);
			sendAll("YUM|" + firstId);
		}

		foods.add(foodId);
		String msg = foodId + " " + getRandomPos() + " " + getRandomVel(FOOD_VELOCITY_SCALE);
		sendAll("FOOD|" + msg);
		foodId++;
	}

	private String getRandomPos() {
		double x = ThreadLocalRandom.current().nextDouble();
		double y = ThreadLocalRandom.current().nextDouble();
		return getPoint(x, y);
	}

	private String getRandomVel(double scale) {
		double theta = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0d;
		double vx = Math.cos(theta) * scale;
		double vy = Math.sin(theta) * scale;
		return getPoint(vx, vy);
	}

	private String getPoint(double x, double y) {
		return BigDecimal.valueOf(x).setScale(4, BigDecimal.ROUND_HALF_UP).toString() + " "
				+ BigDecimal.valueOf(y).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
	}

	private Player getPlayer(int playerId) {
		return players.get(playerId);
	}

	/** Send async message to ALL players. */
	public synchronized void sendAll(String message) {
		for (Player player : players.values()) {
			player.send(message);
		}
	}

	/** Send async message to all players EXCEPT given id. */
	public synchronized void sendAllExcept(String message, int exceptId) {
		for (Player player : players.values()) {
			if (player.getId() != exceptId) {
				player.send(message);
			}
		}
	}
}