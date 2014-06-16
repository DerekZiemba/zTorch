package com.derekziemba.torchplayer;

import java.util.ArrayList;
import java.util.List;
public class BrightnessBehavior {

	private final List<BrightnessTime> steps;
	private boolean error = false;
	
	public BrightnessBehavior(String behaviorScheme) {
		List<BrightnessTime> temp = new ArrayList<BrightnessTime>();
		error = stepExtractor(temp, behaviorScheme);
		this.steps = temp;
	}
	
	public List<BrightnessTime> getSteps() {
		return this.steps;
	}
	
	public String getStepsString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < steps.size(); i++) {
			BrightnessTime bt = steps.get(i);
			sb.append(bt.getLevel() + "/" + fromMillis(bt.getTime()));
			if(i<steps.size()-1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	private static boolean stepExtractor(List<BrightnessTime> result, String behavior) {
		//ex:  1/20m , 4/10m, 9/3.5m, 12/2m, 16/30s
		String scheme = behavior.replaceAll("[^0-9/sm,.]+","");
		
		String[] steps = scheme.split(",");
		boolean foundError = false;
		for(int i=0; i < steps.length; i++){
			BrightnessTime temp = new BrightnessTime(0,0);
			if( stepParser(temp, steps[i]) ) {
				result.add(temp);
			}
			else foundError = true;
		}		
		return foundError;
	}
		
	private static boolean stepParser(BrightnessTime result, String step) {
		String[] lt = step.split("/");
		int level = Integer.valueOf(lt[0].replaceAll("[^0-9]+",""));
		int millis = toMillis(lt[1]);
		if(level >= 0 & level <= TorchConfig.AbsoluteMaxBrightness){
			if(millis > 0) {
				result.set(level,millis);
				return true;
			}
		}
		result.error = "Invalid Brightness Level or Time";
		return false;	
	}
	
	private static int toMillis(String time) {
		int result = 0;
		if(time.contains("m")) {
			time = time.replaceAll("[^0-9.]+","");
			result = (int) (Float.valueOf(time) * 60000); //drops decimal vals
		}
		else if(time.contains("s")) {
			time = time.replaceAll("[^0-9.]+","");
			result = (int) (Float.valueOf(time) * 1000); //drops decimal vals
		}
		else {
			time = time.replaceAll("[^0-9]+","");
			result = Integer.valueOf(time);
		}
		return result;
	}
	
	private static String fromMillis(int time) {
		String result = "error";
		if(time >= 60000) {
			float val = ((float) time / 60000);
			result = valueFormatter(val,2) + "m";
		}
		else if(time >= 1000) {
			float val = ((float) time / 1000);
			result = valueFormatter(val,2)  +"s";
		}
		else {
			result = String.valueOf(time);
		}
		return result;
	}
	
	private static String valueFormatter(float val, int maxDecimalPlaces) {
		return  (val==(int)val ? ""+(int)val : 
			""+Float.valueOf(String.format("%."+maxDecimalPlaces+"f", val)) ); 
	}
	
	
}







