package com.derekziemba.torchplayer;

public class TorchPlayerWorker implements Runnable {
	
	public Thread thread = null;
	private static TorchPlayerWorker tpw = null;
	
	public static enum Options{
		//ADVANCE,
		//REGRESS,
		REPEAT,
		//FALLBACK,
		NOOP
	}

	private BrightnessBehavior behaviorScheme;
	private Options option;

	public void config(String command, Options option) {
		this.behaviorScheme = new BrightnessBehavior(command);
		this.option = option;
	}

	public Thread getThread() {
		return thread;
	}
	
	public void setThread(Thread thread) {
		this.thread = thread;
	}
	
	@Override
	public void run() {		
		BrightnessTime[] steps = behaviorScheme.getSteps().toArray(new BrightnessTime[0]);	
		Boolean stop = false;
		while(!stop && !Thread.interrupted()){
			for( BrightnessTime bt : steps) {
				Torch.setLevelCovertly( bt.getLevel());
				try {
					Thread.sleep(bt.getTime());
				} catch (InterruptedException e) {
					Torch.setLevelCovertly(0);
					return;
				}
			}
			stop = !(Options.REPEAT == option);
		}
		Torch.setLevelCovertly(0);
	}
	
	public static void play(String input) {
		play(input, Options.NOOP);
	}
	
	public static void play(String input, Options option) {
		tpw = new TorchPlayerWorker();
		tpw.setThread(new Thread(tpw));
		tpw.config(input,option);
		tpw.getThread().setPriority(Thread.MAX_PRIORITY);
		tpw.getThread().start();
	}
	
	public static TorchPlayerWorker get() {
		return tpw;
	}
}
