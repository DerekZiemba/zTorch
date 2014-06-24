package com.derekziemba.torchplayer;

import java.util.ArrayList;
import java.util.List;
public class BrightnessBehavior {

	private final List<BrightnessTime> steps;

	public BrightnessBehavior(String behaviorScheme) {
		List<BrightnessTime> temp = new ArrayList<BrightnessTime>();
		InputParsers.stepExtractor(temp, behaviorScheme);
		this.steps = temp;
	}
	
	public List<BrightnessTime> getSteps() {
		return this.steps;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < steps.size(); i++) {
			sb.append(steps.get(i).toString());
			if(i<steps.size()-1) {	sb.append(",");	}
		}
		return sb.toString();
	}
	
	
}







