package com.derekziemba.ztorch;


import com.derekziemba.torchplayer.Torch;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

public class ZTorchWorker extends IntentService{

	public ZTorchWorker() {
		super("ZTorchWorker");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//TorchAction action = EnumSer.getFrom(TorchAction.class,intent);
		int action = Integer.parseInt(intent.getAction());	
		switch(action) {
		case 256:	
			//Torch.setLevelCovertly(1);//Just worry about getting it lit fast as possible, set actual value later
			multiTapHandler();
			break;
		case 1:	
			//Torch.setLevelCovertly(1);//Just worry about getting it lit fast as possible, set actual value later
			Torch.setTorch(this, true);
			break;
		case -1:
			Torch.setTorch(this,false);
			break;
		case 0:
			//Torch.setLevelCovertly(0);//Just worry about shutting it off fast as possible, set actual values later
			Torch.setTorch(this,0);//broadcasts setting
		default:
			break;
		}
		
	}
	
	private void multiTapHandler() {
		boolean doubletap = Z.getBool(this,Z.DOUBLE_TAP);
		boolean rapidtap = Z.getBool(this,Z.RAPID_TAP);
		if(doubletap || rapidtap) {			
			long time = System.currentTimeMillis(); 
			long lasttime = Z.getLong(this,Z.TEMP_TIME);
			int period = Z.getInt(this,Z.TAP_TIME);
			Z.putLong(this, Z.TEMP_TIME, time);
			int level = Z.getInt(this,Torch.CURRENT_BRIGHTNESS);			
			if(lasttime+period >= time) {								
				if(doubletap) {
					int defaultLevel = Z.getInt(this,Z.DEFAULT_BRIGHTNESS); 
					if(level == 0) { 	level = 1;	}
					else if(level != defaultLevel) {	level = defaultLevel; 	}
					else if(level == defaultLevel) {	level = 0;	}
					Torch.setTorch(this,level);
				}
				else if(rapidtap) {	Torch.setTorch(this,true);	}
			}
			else {
				if(level == 0) {	Torch.setTorch(this,1);	}
				else if(level != 0) {	Torch.setTorch(this,0);	}
			}
		}
		else {
			if(Z.getInt(this,Torch.CURRENT_BRIGHTNESS)==0) {
				Torch.setTorch(this, Z.getInt(this,Z.DEFAULT_BRIGHTNESS));
			} 
			else {	Torch.setTorch(this,0);	}
		}
	}

	
	public static PendingIntent getWidgetIntent(Context context, int action, int appWidgetId) {
		Intent intent = new Intent(context, ZTorchWorker.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(String.valueOf(action));
		//EnumSer.putTo(action,intent);
		if(appWidgetId != -1) {	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);	}
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public static PendingIntent getNotifIntent(Context context,int action) {
		Intent intent = new Intent(context, ZTorchWorker.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(String.valueOf(action));
		//EnumSer.putTo(action,intent);
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
