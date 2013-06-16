package com.application.actify.view.component;

public class BlockData {
	private float x;
	private float yStart;
	private float yEnd;
	private int color;
	
	public BlockData(float x, float yStart, float yEnd, int color) {
		super();
		this.x = x;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.color = color;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getyStart() {
		return yStart;
	}

	public void setyStart(float yStart) {
		this.yStart = yStart;
	}

	public float getyEnd() {
		return yEnd;
	}

	public void setyEnd(float yEnd) {
		this.yEnd = yEnd;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	
}
