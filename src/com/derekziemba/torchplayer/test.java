package com.derekziemba.torchplayer;

import java.util.HashMap;


public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, BrightnessBehavior> behaviorSchemes = new HashMap<String,BrightnessBehavior>(4);
		
		BrightnessBehavior x = new BrightnessBehavior("1/1000, 9/1000, 0/1000, 5/.5s, 0/.5s, 5/.5s, 0/300, 1/2.5s");
		behaviorSchemes.put("example", x );

		System.out.println(behaviorSchemes.get("example").getStepsString());
	}
}
