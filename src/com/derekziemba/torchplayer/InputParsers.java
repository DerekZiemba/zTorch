package com.derekziemba.torchplayer;

import java.util.List;

public class InputParsers {

	static boolean stepExtractor(List<BrightnessTime> result, String behavior) {
		//ex:  1/20m , 4/10m, 9/3.5m, 12/2m, 16/30s
		String scheme = behavior.replaceAll("[^0-9/sm,.*&]+","");
				
		String[] steps = scheme.split(",");
		boolean foundError = false;
		for(int i=0; i < steps.length; i++){
			String temp = steps[i];
			if(temp.contains("&")){
				String multiplier = temp.substring(temp.indexOf("*")+1, temp.length());
				int multi = Integer.valueOf(multiplier);			
				temp = temp.substring(0, temp.indexOf("*"));			
				String[] terms = temp.split("&");
				for(int j=0; j<multi; j++) {
					for(int k=0; k<terms.length; k++) {
						BrightnessTime bt = new BrightnessTime(0,0);
						if( InputParsers.stepParser(bt, terms[k]) ) {
							result.add(bt);
						}
						else foundError = true;
					}
				}
			}
			else {
				int loop = 1;
				if(steps[i].contains("*")){
					String amount = steps[i].substring(steps[i].indexOf("*")+1);
					steps[i] = steps[i].replace(amount,"");
					loop = Integer.valueOf(amount);
				}
				for(int j=0; j<loop; j++) {
					BrightnessTime bt = new BrightnessTime(0,0);
					if( InputParsers.stepParser(bt, steps[i]) ) {
						result.add(bt);
					}
					else foundError = true;
				}
			}
		}
		return foundError;
	}

	static boolean stepParser(BrightnessTime result, String input) {
		String step = input.replaceAll("[^0-9sm./]+","");
		String[] lt = step.split("/");
		if(lt.length >2) {
			return false;
		}
		int level = Integer.valueOf(lt[0].replaceAll("[^0-9]+",""));
		int millis = InputParsers.toMillis(lt[1]);
		if(level >= 0 & level <= Torch.AbsMaxLvl){
			if(millis > 0) {
				result.set(level,millis);
				return true;
			}
		}
		return false;	
	}

	static int toMillis(String time) {
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

	static String fromMillis(int time) {
		String result = "error";
		if(time >= 60000) {
			float val = ((float) time / 60000);
			result = InputParsers.valueFormatter(val,2) + "m";
		}
		else if(time >= 1000) {
			float val = ((float) time / 1000);
			result = InputParsers.valueFormatter(val,2)  +"s";
		}
		else {
			result = String.valueOf(time);
		}
		return result;
	}

	static String valueFormatter(float val, int maxDecimalPlaces) {
		return  (val==(int)val ? ""+(int)val : 
			""+Float.valueOf(String.format("%."+maxDecimalPlaces+"f", val)) ); 
	}
    
}

