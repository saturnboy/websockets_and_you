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
	/** How many players need to start a game. */
	private static final int PLAYER_START_SIZE = 2;

	/** Min number of players, game will end if number drops below this. */
	private static final int PLAYER_MIN = 2;

	/** Game ends when a player reaches the max score. */
	private static final int SCORE_MAX = 99;

	/** Max amount of uneaten food. When new food spawns, the oldest food is removed. */
	private static final int FOOD_MAX = 7;

	/** Spawn rate of food (in seconds). */
	private static final int FOOD_SPAWN_INTERVAL = 2;

	/** After start, wait this long before spawing first food (in seconds). */
	private static final int FOOD_SPAWN_DELAY = 5;

	/** Food velocity scale factor (in points). */
	private static final double FOOD_VELOCITY_SCALE = 25.0d;

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
	public synchronized void onJoin(FeedSocket socket) {
		Player player = new Player(socket);
		player.setPos(getRandomPos());
		players.put(player.getId(), player);
		player.send("WELCOME|" + player.getId() + " " + player.getPos());

		// make sure everyone knows about everyone else
		for (Player p : players.values()) {
			sendAllExcept("OTHER|" + p.getId() + " " + p.getPos(), p.getId());
		}

		if (players.size() >= PLAYER_START_SIZE) {
			// start the game
			if (!started) {
				started = true;
				sendAll("START|" + System.currentTimeMillis());

				executor.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						doSpawnFood();
					}
				}, FOOD_SPAWN_DELAY, FOOD_SPAWN_INTERVAL, TimeUnit.SECONDS);
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
		executor.shutdown();
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

	/** Send async message to all players EXCEPT the given id. */
	public synchronized void sendAllExcept(String message, int exceptId) {
		for (Player player : players.values()) {
			if (player.getId() != exceptId) {
				player.send(message);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("GAME : ");
		sb.append(started ? "started" : "not started").append(" : ");
		sb.append(players.size()).append(" player").append(players.size() == 1 ? "\n" : "s\n");

		for (Player p : players.values()) {
			sb.append("  ").append(p.toString()).append("\n");
		}

		return sb.substring(0, sb.length() - 1);
	}
}