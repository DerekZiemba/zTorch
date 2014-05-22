package com.derekziemba.ztorch;

import com.derekziemba.ztorch.providers.Notifier;

public class Global {
	public static final String PACKAGE_NAME = Global.class.getPackage().getName();
	
	public static final String FILENAME = "zTorch_Preferences";

	public static final String FLASH_VALUE_UPDATED_BROADCAST_NAME = "com.derekziemba.ztorch.FLASH_VALUE_UPDATED";
	public static final String KEY_NEW_VALUE = "new_value";

	private static final String SETTINGS_DEFAULT_BRIGHTNESS = "flash_default_value";
	public static final String SETTINGS_ENABLE_DOUBLE_TAP = "widget_double_tap";
	public static final String SETTINGS_ENABLE_RAPID_TAP = "widget_rapid_tap";
	public static final String SETTINGS_TAP_TIME = "widget_rapid_tap_time";
	public static final String TEMP_TIME = "temp_time";
	public static final String SETTINGS_ENABLE_PERSISTENT_NOTIF = "persistent_notification";
	public static final String SETTINGS_ENABLE_MINIMIZE_NOTIF = "minimize_notification";	
	
	public static final int mNotificationId = 1;
	public static final int StockBrightness = 4;
	public static final int default_steps_increments = 4;
	public static final int default_tap_time = 800;
	public static final int max_tap_time = 3000;

	public static final boolean debugmode = true;
	
	public static void setNotification(Context context, int level) {
		boolean persist = Global.getPersistNotif(context);
		boolean minimize = Global.getMinimizeNotif(context);
		
		Notifier note = new Notifier(context, persist, minimize,  level);
		note.create();
	}
		
	public static boolean getPersistNotif(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_ENABLE_PERSISTENT_NOTIF,false);
	}
	public static void setPersistNotif(Context context, boolean value) {
		NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancelAll();
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Global.SETTINGS_ENABLE_PERSISTENT_NOTIF, value).commit();
	}	
	
	public static boolean getMinimizeNotif(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_ENABLE_MINIMIZE_NOTIF,false);
	}
	public static void setMinimizeNotif(Context context, boolean value) {
		NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancelAll();
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Global.SETTINGS_ENABLE_MINIMIZE_NOTIF, value).commit();
	}	

	public static int getDefaultBrightness(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(SETTINGS_DEFAULT_BRIGHTNESS,StockBrightness);
	}
	public static void setDefaultBrightness(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Global.SETTINGS_DEFAULT_BRIGHTNESS, value).commit();
	}
	
	public static boolean getDoubleTap(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_ENABLE_DOUBLE_TAP,false);
	}
	public static void setDoubleTap(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Global.SETTINGS_ENABLE_DOUBLE_TAP, value).commit();
	}
	
	public static boolean getRapidTap(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_ENABLE_RAPID_TAP,false);
	}
	public static void setRapidTap(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Global.SETTINGS_ENABLE_RAPID_TAP, value).commit();
	}
	
	public static int getTapTime(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(SETTINGS_TAP_TIME,default_tap_time);
	}
	public static void setTapTime(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Global.SETTINGS_TAP_TIME, value).commit();
	}
	
	public static boolean getWidgetQuickTap(Context context) {
		if(getRapidTap(context) || getDoubleTap(context)) {	return true;	}
		return false;
	}
	
	
	public static long getLastTime(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(TEMP_TIME,default_tap_time);
	}
	public static void setLastTime(Context context, long value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(Global.TEMP_TIME, value).commit();
	}
	
}


