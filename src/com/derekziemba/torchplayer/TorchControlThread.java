package com.derekziemba.torchplayer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

public class TorchControlThread implements Runnable {

	BrightnessBehavior behaviorScheme;
	
	public TorchControlThread(String command) {
		this.behaviorScheme = new BrightnessBehavior(command);
	}
	
	@Override
	public void run() {		
		Shell shell = getShell();
		String flashfile = TorchConfig.getSysFsFile();

		for( BrightnessTime bt : behaviorScheme.getSteps()) {
			String commandString = 	"echo " + String.valueOf(bt.getLevel()) + " > "+ flashfile;	
			CommandCapture command = new CommandCapture(0, commandString);
			
			try {	shell.add(command);
			} catch (IOException e) {	e.printStackTrace();	}		
			
			sleep(bt.getTime());
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

	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
