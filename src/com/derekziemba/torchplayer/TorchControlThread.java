package com.derekziemba.torchplayer;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;


 
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
		Shell shell = RootTools.getExistingShell();
		Boolean stop = false;
		int cmdcount = 0;
		while(!stop && !Thread.interrupted()){
			for( BrightnessTime bt : steps) {
				shell.add(new CommandCapture(cmdcount, "echo " + bt.getLevel() + " > "+ flashfile));
				try {
					Thread.sleep(bt.getTime());
				} catch (InterruptedException e) {
					shell.add(new CommandCapture(cmdcount+1, "echo " + 0 + " > "+ flashfile));
					return;
				}
				cmdcount++;
			}
			stop = !(Options.REPEAT == option);
		}
		shell.add(new CommandCapture(cmdcount, "echo " + 0 + " > "+ flashfile));
	}

}
