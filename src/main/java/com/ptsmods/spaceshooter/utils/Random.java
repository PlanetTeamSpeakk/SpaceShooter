package com.ptsmods.spaceshooter.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Actually random, unlike ThreadLocalRandom, smh. Oracle, pls.
 * @author PlanetTeamSpeak
 */
public class Random {

	private static final Character[] characters = {
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	private Random() { }

	public static int randInt() {
		return randInt(0, Integer.MAX_VALUE);
	}

	public static int randInt(int max) {
		return randInt(0, max);
	}

	public static int randInt(int min, int max) {
		return (int) randDouble(min, max);
	}

	public static long randLong() {
		return randLong(0, Long.MAX_VALUE);
	}

	public static long randLong(long max) {
		return randLong(0, max);
	}

	public static long randLong(long min, long max) {
		return (long) randDouble(min, max);
	}

	public static short randShort() {
		return randShort((short) 0, Short.MAX_VALUE);
	}

	public static short randShort(short max) {
		return randShort((short) 0, max);
	}

	public static short randShort(short min, short max) {
		return (short) randLong(min, max);
	}

	public static double randDouble() {
		return randDouble(0D, Double.MAX_VALUE);
	}

	public static double randDouble(double max) {
		return randDouble(0D, max);
	}

	public static double randDouble(double min, double max) {
		return randDouble(min, max, true);
	}

	public static double randDouble(double min, double max, boolean useSeeding) {
		double rng = (Math.random() * max + min) * (min < 0D ? (int) (Math.random() * 10) >= 5 ? 1 : -1 : 1);
		if (rng > max) rng = max;
		if (rng < min) rng = min;
		return rng;
	}

	public static float randFloat() {
		return randFloat(0F, Float.MAX_VALUE);
	}

	public static float randFloat(float max) {
		return randFloat(0F, max);
	}

	public static float randFloat(float min, float max) {
		return (float) randDouble(min, max);
	}

	public static <T> T choice(T... choices) {
		return choices[randInt(choices.length)];
	}

	public static <T> T choice(List<T> choices) {
		return choices.get(randInt(choices.size()));
	}

	public static <T> void scramble(List<T> list) {
		List<T> listCopy = new ArrayList(list);
		list.clear();
		List<T> passed = new ArrayList();
		for (int x = 0; x < listCopy.size(); x++) {
			T chosenOne = choice(listCopy);
			while (passed.contains(chosenOne)) chosenOne = choice(listCopy);
			list.add(chosenOne);
			passed.add(chosenOne);
		}
	}

	public static String genKey(int length) {
		String key = "";
		for (int i : new int[length])
			key += choice(characters);
		return key;
	}

}
