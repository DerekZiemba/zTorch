package com.derekziemba.ztorch;


public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {	
			TorchConfig.setTorch(context,0);				
		} 
		
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {	
			TorchConfig.setTorch(context,0);	
		}
	}
}