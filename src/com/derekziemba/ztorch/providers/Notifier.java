package com.derekziemba.ztorch.providers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.derekziemba.ztorch.Z;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.ResultsService;
import com.derekziemba.ztorch.activities.MainActivity;

@SuppressLint({ "InlinedApi", "NewApi" })
public class Notifier {

	private Context context = null;
	private boolean persist = false;
	private boolean minimize = false;
	private int level = 0;
	
	private NotificationManager mgr = null; 
	private NotificationCompat.Builder builder = null;
	private Intent openMainApp = null;
	private PendingIntent pending = null;
		
	/*
	 * Available Priorities:
	 * PRIORITY_MAX = for your application's most important items that require the user's prompt attention or input.
	 * PRIORITY_HIGH = for more important notifications or alerts.
	 * PRIORITY_LOW = for items that are less important.
	 * PRIORITY_MIN = hese items might not be shown to the user except under special circumstances, such as detailed notification logs.
	 */
	
	int priority = Notification.PRIORITY_LOW; //Default
	
	public Notifier(Context context,  boolean persist, boolean minimize, int level) {
		this.context = context;
		this.persist = persist;
		this.minimize = minimize;
		this.level = level;		
	}
		
	public void create() {
		determinePriority();
		mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if(level >0) {	priorityMax();	}
		else if(persist) {	
			priorityLow();	
		}
		else {
			mgr.cancelAll();
		}
	}
	
	private void determinePriority() {
		if(level >0) {		priority = Notification.PRIORITY_MAX;	}
		else if(persist) {	
			if(minimize) {	priority =  Notification.PRIORITY_MIN;	}
			else {	priority = Notification.PRIORITY_LOW;	}
		}
	}


	private void common() {
		builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(context.getString(R.string.app_name)+ "    Brightness: "  + level);
		builder.setContentText(context.getString(R.string.notification_text_tap_to_open));
		openMainApp = new Intent(context, MainActivity.class);
		openMainApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pending = PendingIntent.getActivity(context, 0, openMainApp, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	
	private void priorityMax() {  //Notification needs to be front and center because app is activated
		common();
		builder.setTicker(context.getString(R.string.ticker_text));
		
		if(persist) {
			builder.addAction(R.drawable.ic_stat_minus, "OFF", getPendingIntent(context, 0));
		}
		else {
			builder.addAction(R.drawable.ic_stat_minus, "CLOSE", getPendingIntent(context, 0));
		}
		builder.addAction(R.drawable.ic_stat_minus, "", getPendingIntent(context, -1));
		builder.addAction(R.drawable.ic_stat_plus, "", getPendingIntent(context, 1));;
		publish();
	}
	
	private void priorityLow() {  //Persists is on but not hidden
		common();
		builder.addAction(R.drawable.ic_stat_plus, "TURN ON", getPendingIntent(context, 1));;
		publish();
	}
		
	private void publish() {
		builder.setOngoing(true);
		if (Build.VERSION.SDK_INT >= 16) {	builder.setPriority(priority);	}
		builder.setContentIntent(pending);
		Notification notification = builder.build();
		if (Build.VERSION.SDK_INT >= 16) {	notification.priority = priority;	}
		mgr.notify(Z.mNotificationId, notification);
	}
	

	private static PendingIntent getPendingIntent(Context context, int action) {
		Intent intent = new Intent(context, ResultsService.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(String.valueOf(action));
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}	
	



}












