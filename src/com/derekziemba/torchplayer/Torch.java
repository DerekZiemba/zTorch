package com.derekziemba.torchplayer;

import java.io.File;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.derekziemba.misc.Shell;
import com.derekziemba.ztorch.Z;

public class Torch 
{
	private static final boolean ignoreDeviceValidity = true; //for testing in emulator
	
	private static String FLASH_FILE = null;

	private static final String[] listOfFlashFiles = {
			"/sys/class/camera/flash/rear_flash",
			"/sys/class/camera/rear/rear_flash",
			"/sys/class/leds/flashlight/brightness",
			"/sys/devices/platform/flashlight.0/leds/flashlight/brightness",
			"/sys/class/leds/spotlight/brightness",
			"/sys/class/leds/torch-flash/flash_light" 
	};

	public static final int AbsMaxLvl = 16;
	public static final String CURRENT_BRIGHTNESS = "flash_current_value";
	public static final String LIMIT_VALUE = "flash_limit_value";
	public static final String INCREMENTS = "brightness_increment_steps";

	private static void torchStatusBroadcast(Context context, int value) {		
		Intent broadcastIntent =new Intent(Z.FLASH_VALUE_UPDATED_BROADCAST_NAME);
		broadcastIntent.putExtra(CURRENT_BRIGHTNESS, value);
		context.sendBroadcast(broadcastIntent);
		setCurrentBrightnessPref(context, value);
		Z.setNotif(context, value);
	}

	/*******************************************************************************
	 * Getters and setters
	 *******************************************************************************/
	private static void setCurrentBrightnessPref(Context context, int value) {
		int last = Z.getInt(context,Torch.CURRENT_BRIGHTNESS);
		if( (last == 0) ^ (value == 0) ) {
			NotificationManager mgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mgr.cancelAll();
		}
		Z.sharedPrefs(context).edit().putInt(CURRENT_BRIGHTNESS, value).commit();
	}

	public static int setTorch(Context context, boolean increase) {
		return setTorch(context, incrementedTorchValue(context, increase));
	}
	
	public static int setTorch(Context context, int value) {
		if (checkValidity(context, value)) {
			setLevelCovertly(value);
			torchStatusBroadcast(context, value);
		}
		return value; //Allows the setTorch boolean method to return the value that was set
	}
	
	
	/**
	 * Bypasses all checks and settings and just sets teh value.  The app will not know what the level is because it is not recorded.
	 * Will not trigger broadcast, widget views, notification
	 * @param value
	 */
	public static void setLevelCovertly(int value) {	Shell.exec("echo " + value + " > "+ getSysFsFile());	}


	/*******************************************************************************
	 * Privates For Changing The Torch Brightness
	 *******************************************************************************/
	private static int incrementedTorchValue(Context context, boolean increase) {
		int brightness = Z.getInt(context,Torch.CURRENT_BRIGHTNESS);
		int maxBright = Z.getInt(context,Torch.LIMIT_VALUE);
		int increment = Z.getInt(context,Torch.INCREMENTS);
		return incrementLogic(brightness, maxBright, increment, increase);
	}

	private static int incrementLogic(	
			int brightness, 
			int maxBright,
			int increment, 
			boolean increase) 
	{
		if (increase) 
		{			
			if (brightness == 0) {
				return 1;
			}
			if (brightness > maxBright) {
				return maxBright;
			}
			if (brightness == 1) {
				brightness = 0;
			}
			brightness += increment;
			if (brightness > maxBright) {
				return maxBright;
			}
			if (brightness > maxBright - (increment / 2)) {
				return maxBright;
			}
			return brightness;
		} 
		else {
			if (brightness == 0) {
				return 0;
			}
			if (brightness == 1) {
				return 0;
			}
			brightness -= increment;
			if (brightness <= 2) {
				return 1;
			}
			return brightness;
		}
	}

	public static boolean checkValidity(Context c, int value) {
		if (value > AbsMaxLvl | value < 0) 	{	return false;	}
		
		if(ignoreDeviceValidity) return ignoreDeviceValidity; //for android emulator
		
		if(getSysFsFile()!=null) {	return true;	
		}	else {	Toast.makeText(c, "UNSUPPORTED OR INVALID: \n-DEVICE", Toast.LENGTH_SHORT).show();	}
		return false;
	}
	
	public static String getSysFsFile() {
		if (FLASH_FILE != null)  return FLASH_FILE;
		for (String filePath : listOfFlashFiles) {
			File flashFile = new File(filePath);
			if (flashFile.exists()) {	FLASH_FILE = filePath;	}
		}
		return FLASH_FILE;
	}
}


