package com.derekziemba.torchplayer;

import com.derekziemba.root.Shell;


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
		Shell shell = Shell.getShell();
		Boolean stop = false;
		while(!stop && !Thread.interrupted()){
			for( BrightnessTime bt : steps) {
				shell.exec("echo " + bt.getLevel() + " > "+ flashfile);
				try {
					Thread.sleep(bt.getTime());
				} catch (InterruptedException e) {
					shell.exec("echo " + 0 + " > "+ flashfile);
					return;
				}
			}
			stop = !(Options.REPEAT == option);
		}
		shell.exec("echo " + 0 + " > "+ flashfile);
	}
	


	
	
}
