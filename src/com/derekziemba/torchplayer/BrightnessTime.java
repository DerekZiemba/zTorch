package com.derekziemba.torchplayer;

public class BrightnessTime {
	
	private int brightness;
	private int time;
	
	public BrightnessTime(int level, int milliseconds) {
		this.brightness = level;
		this.time = milliseconds;
	}
	
	public int getLevel() { return this.brightness; }	
	public int getTime() { return this.time; }
	
	public void setLevel(int b) { this.brightness = b; }	
	public void setTime(int l) { this.time = l; }

	
	public void set(int level, int milliseconds) {
		this.brightness = level;
		this.time = milliseconds;
	}
	
	public String toString() {
		return getLevel() + "/" + InputParsers.fromMillis(getTime());
	}
		
}
