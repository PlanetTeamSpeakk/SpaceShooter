package com.ptsmods.spaceshooter.utils;

import java.awt.Point;

public class Vec2i {

	private volatile int	x;
	private volatile int	y;

	public Vec2i(Point parent) {
		this((int) parent.getX(), (int) parent.getY());
	}

	public Vec2i(Vec2i parent) {
		this(parent.x, parent.y);
	}

	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void add(int x, int y) {
		this.x += x;
		this.y += y;
	}

	public void subtract(int x, int y) {
		this.x -= x;
		this.y -= y;
	}

	/**
	 * Returns the current x value
	 * 
	 * @return the current x value.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the current y value
	 * 
	 * @return the current y value.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the current x value to the given int.
	 * 
	 * @return the previous x value.
	 */
	public int setX(int x) {
		int prev = this.x;
		this.x = x;
		return prev;
	}

	/**
	 * Sets the current y value to the given int.
	 * 
	 * @return the previous y value.
	 */
	public int setY(int y) {
		int prev = this.y;
		this.y = y;
		return prev;
	}

	/**
	 * Adds the given int to the current x value.
	 * 
	 * @return the previous x value.
	 */
	public int addX(int x) {
		int prev = this.x;
		this.x += x;
		return prev;
	}

	/**
	 * Adds the given int to the current y value.
	 * 
	 * @return the previous y value.
	 */
	public int addY(int y) {
		int prev = this.y;
		this.y += y;
		return prev;
	}

	/**
	 * Subtracts the given int from the current x value.
	 * 
	 * @return the previous x value.
	 */
	public int subtractX(int x) {
		int prev = this.x;
		this.x -= x;
		return prev;
	}

	/**
	 * Subtracts the given int from the current y value.
	 * 
	 * @return the previous y value.
	 */
	public int subtractY(int y) {
		int prev = this.y;
		this.y -= y;
		return prev;
	}

	/**
	 * Returns the x value, but always as a positive value.
	 * 
	 * @return the x value, but always as a positive value.
	 */
	public int getXPositive() {
		return x < 0 ? -x : x;
	}

	/**
	 * Returns the y value, but always as a positive value.
	 * 
	 * @return the y value, but always as a positive value.
	 */
	public int getYPositive() {
		return y < 0 ? -y : y;
	}

	/**
	 * Returns the x value, but always as a negative value.
	 * 
	 * @return the x value, but always as a negative value.
	 */
	public int getXNegative() {
		return x > 0 ? -x : x;
	}

	/**
	 * Returns the y value, but always as a negative value.
	 * 
	 * @return the y value, but always as a negative value.
	 */
	public int getYNegative() {
		return y > 0 ? -y : y;
	}

}
