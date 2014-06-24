package com.derekziemba.ztorch;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.derekziemba.ztorch.providers.NotificationProvider;

public class Z {

	public static final String FILENAME = "zTorch_Preferences";
	public static final String FLASH_VALUE_UPDATED_BROADCAST_NAME = "com.derekziemba.ztorch.FLASH_VALUE_UPDATED";


	public static final String DEFAULT_BRIGHTNESS = "flash_default_value";
	public static final String DOUBLE_TAP = "widget_double_tap";
	public static final String RAPID_TAP = "widget_rapid_tap";
	public static final String TAP_TIME = "widget_rapid_tap_time";
	public static final String TEMP_TIME = "temp_time";
	public static final String PERSISTENT_NOTIF = "persistent_notification";
	public static final String MINIMIZE_NOTIF = "minimize_notification";	
	
	public static final int mNotificationId = 1;
	public static final int StockBrightness = 4;
	public static final int default_steps_increments = 4;
	public static final int default_tap_time = 800;
	public static final int max_tap_time = 3000;

	public static final boolean debugmode = true;
	
	public static void cancelNotif(Context context) {
		NotificationManager mgr = 
			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancelAll();
	}
	
	public static void setNotif(Context context, int level) {
		boolean persist = Z.getBool(context,Z.PERSISTENT_NOTIF);
		boolean minimize = Z.getBool(context,Z.MINIMIZE_NOTIF);
		NotificationProvider note = 
			new NotificationProvider(context, persist, minimize,  level);
		note.create();
	}


	public static SharedPreferences sharedPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static void putInt(Context context, String key, int value) {
		Z.sharedPrefs(context).edit().putInt(key,value).commit();
	}

	public static int getInt(Context context, String key) {
		return getInt(context,key,0);
	}
	
	public static int getInt(Context context, String key, int def) {
		return Z.sharedPrefs(context).getInt(key,def);
	}
	
	public static void putLong(Context context, String key, long value) {
		Z.sharedPrefs(context).edit().putLong(key,value).commit();
	}

	public static long getLong(Context context, String key) {
		return getLong(context,key,0);
	}
	
	public static long getLong(Context context, String key, long def) {
		return Z.sharedPrefs(context).getLong(key,def);
	}
	
	public static void putBool(Context context, String key, boolean value) {
		Z.sharedPrefs(context).edit().putBoolean(key,value).commit();
	}
	
	public static Boolean getBool(Context context, String key) {
		return getBool(context, key, false);
	}
	
	public static Boolean getBool(Context context, String key, boolean def) {
		return Z.sharedPrefs(context).getBoolean(key,def);
	}
	
}










