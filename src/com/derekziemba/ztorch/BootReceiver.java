package com.derekziemba.ztorch;

import com.derekziemba.torchplayer.Torch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {	
			Torch.setLevelCovertly(0);				
		} 
		
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {	
			Torch.setLevelCovertly(0);	
		}
	}
}