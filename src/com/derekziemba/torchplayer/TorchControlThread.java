package com.derekziemba.torchplayer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;



import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
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
	private Shell shell;
	private String flashfile;
	private Options option;
	


	public void config(String command, Options option) {
		this.behaviorScheme = new BrightnessBehavior(command);
		this.option = option;
	}
	
	@Override
	public void run() {		
		shell = getShell();
		flashfile = TorchConfig.getSysFsFile();
		BrightnessTime[] steps = behaviorScheme.getSteps().toArray(new BrightnessTime[0]);	
		Boolean stop = false;
		while(!stop && !Thread.interrupted()){
			for( BrightnessTime bt : steps) {
				
				execute(shell, flashfile, bt.getLevel());
				
				try {
					Thread.sleep(bt.getTime());
				} catch (InterruptedException e) {
					execute(shell,flashfile,0);
					return;
				}
			}
			stop = !(Options.REPEAT == option);
		}
		execute(shell,flashfile,0);
	}

	private void execute(Shell shell, String file, int level) {
		try {	
			String commandString = 	"echo " + level + " > "+ file;	
			shell.add(new CommandCapture(0, commandString));
		} catch (IOException e) {	
			e.printStackTrace();	
		}	
	}
	
	private Shell getShell() {
		try {
			return RootTools.getShell(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RootDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
