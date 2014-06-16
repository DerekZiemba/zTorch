package com.derekziemba.ztorch;

import com.derekziemba.torchplayer.TorchConfig;

import android.app.IntentService;
import android.content.Intent;


public class ResultsService extends IntentService {
	public ResultsService() {
		super("ResultsService");
	}

	public ResultsService(String name) {
		super(name);
	}

	/**
	 * Action 256 will set the torch to user default brightness or shut it off if already there
	 * Action 1 will brighten the torch
	 * Action 0 will disable the torch
	 * action -1 will decrease the torch

	 */
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		int action = Integer.parseInt(intent.getAction());	
		
		if (action == -1) {
			TorchConfig.setTorch(getApplicationContext(), false);
		} 
		else if (action == 0) {
			TorchConfig.setTorch(getApplicationContext(),0);
		} 
		else if (action == 1) {
			TorchConfig.setTorch(getApplicationContext(), true);
		} 
		else if(action == 256) {
			multiTapHandler();
		}
	}
	
	private void multiTapHandler() 
	{
		boolean doubletap = Global.getDoubleTap(this);
		boolean rapidtap = Global.getRapidTap(this);		
		if(doubletap || rapidtap) {			
			long time = System.currentTimeMillis();
			long lasttime = Global.getLastTime(getApplicationContext());
			int period = Global.getTapTime(getApplicationContext());
			Global.setLastTime(getApplicationContext(), time);
			int level = TorchConfig.getCurrentBrightness(this);			
			if(lasttime+period >= time) 
			{								
				if(doubletap) 
				{
					int defaultLevel = Global.getDefaultBrightness(this); 
					if(level == 0) { 	level = 1;	}
					else if(level != defaultLevel) {	level = defaultLevel; 	}
					else if(level == defaultLevel) {	level = 0;	}
					TorchConfig.setTorch(this,level);
				}
				else if(rapidtap) {	TorchConfig.setTorch(this,true);	}
			}
			else {
				if(level == 0) {	TorchConfig.setTorch(this,1);	}
				else if(level != 0) {	TorchConfig.setTorch(this,0);	}
			}
		}
		else {
			if(TorchConfig.getCurrentBrightness(getApplicationContext())==0) 
			{
				TorchConfig.setTorch(getApplicationContext(), Global.getDefaultBrightness(this));
			} 
			else {	TorchConfig.setTorch(getApplicationContext(),0);	}
		}
	}
}
