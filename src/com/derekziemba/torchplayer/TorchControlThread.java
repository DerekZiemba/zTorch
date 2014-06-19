package com.derekziemba.torchplayer;

import java.io.IOException;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;


 
public class TorchControlThread implements Runnable {
	
	public static enum Options{
		ADVANCE,
		REGRESS,
		REPEAT,
		FALLBACK,
		NOOP
	}

	private BrightnessBehavior behaviorScheme;
	private String flashfile;
	private Options option;
	


	public void config(String command, Options option) {
		this.behaviorScheme = new BrightnessBehavior(command);
		this.option = option;
	}
	
	@Override
	public void run() {		
		flashfile = TorchConfig.getSysFsFile();
		BrightnessTime[] steps = behaviorScheme.getSteps().toArray(new BrightnessTime[0]);	
		Boolean stop = false;
		while(!stop && !Thread.interrupted()){
			for( BrightnessTime bt : steps) {
				
				execute( flashfile, bt.getLevel());
				
				try {
					Thread.sleep(bt.getTime());
				} catch (InterruptedException e) {
					execute(flashfile,0);
					return;
				}
			}
			stop = !(Options.REPEAT == option);
		}
		execute(flashfile,0);
	}

	private void execute(String file, int level) {
		try {	
			String commandString = 	"echo " + level + " > "+ file;	
			RootTools.getExistingShell().add(new CommandCapture(0, commandString));
		} catch (IOException e) {	
			e.printStackTrace();	
		}	
	}

}
