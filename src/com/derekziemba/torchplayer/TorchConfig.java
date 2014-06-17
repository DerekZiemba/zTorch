package com.derekziemba.torchplayer;


import java.io.File;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.derekziemba.ztorch.Z;
import com.derekziemba.ztorch.activities.MainActivity;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

public class TorchConfig 
{
	private static final boolean ignoreDeviceValidity = true; //for testing in emulator
	
	private static String FLASH_FILE = null;
	private static final String SETTINGS_ROOT_ACCESS = "root_access_granted";
	private static final String SETTINGS_VALID_SYSFILE = "valid_sysfs_file";
	private static final String[] listOfFlashFiles = 
	{
			"/sys/class/camera/flash/rear_flash",
			"/sys/class/camera/rear/rear_flash",
			"/sys/class/leds/flashlight/brightness",
			"/sys/devices/platform/flashlight.0/leds/flashlight/brightness",
			"/sys/class/leds/spotlight/brightness",
			"/sys/class/leds/torch-flash/flash_light" 
	};

	public static final int AbsoluteMaxBrightness = 16;
	private static final String SETTING_CURRENT_BRIGHTNESS = "flash_current_value";
	private static final String SETTING_LIMIT_VALUE = "flash_limit_value";
	private static final String SETTING_INCREMENTS_STEPS = "brightness_increment_steps";

	/**
	 * Below function is application dependent and doesn't belong in this class
	 * But with my level of experience I don't know how to make it work any other way
	 * @param context
	 * @param value
	 */
	private static void torchStatusBroadcast(Context context, int value) 
	{
		setCurrentBrightnessPref(context, value);
		Intent broadcastIntent =
				new Intent(Z.FLASH_VALUE_UPDATED_BROADCAST_NAME);
		broadcastIntent.putExtra(Z.KEY_NEW_VALUE, value);
		context.sendBroadcast(broadcastIntent);
		Z.setNotification(context, value);
	}

	/*******************************************************************************
	 * Getters and setters
	 *******************************************************************************/
	
	public static int getAbsMaxBrightness() 
	{
		return AbsoluteMaxBrightness;
	}
	

	public static int getCurrentBrightness(Context context) 
	{
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getInt(SETTING_CURRENT_BRIGHTNESS, 0);
	}
	

	private static void setCurrentBrightnessPref(Context context, int value) 
	{
		int last = getCurrentBrightness(context);
		if( (last == 0) ^ (value == 0) ) 
		{
			NotificationManager mgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mgr.cancelAll();
		}
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit()
			.putInt(SETTING_CURRENT_BRIGHTNESS, value)
			.commit();
	}
	

	public static int getBrightnessLimitValue(Context context) 
	{
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getInt(SETTING_LIMIT_VALUE, AbsoluteMaxBrightness);
	}

	public static void setBrightnessLimitValue(Context context, int value) 
	{
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit()
			.putInt(SETTING_LIMIT_VALUE, value)
			.commit();
	}

	public static int getIncrementSteps(Context context) 
	{
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getInt(SETTING_INCREMENTS_STEPS, 6);
	}

	public static void setIncrementSteps(Context context, int value) 
	{
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putInt(SETTING_INCREMENTS_STEPS, value).commit();
	}

	/**
	 * SetTorch is the one method needed to set the torch to a value. Value can
	 * be an number or boolean to increment
	 * 
	 * @param context
	 * @param value
	 * @return brightness level
	 */
	public static int setTorch(Context context, int value) 
	{
		if (checkValidity(context, value)) 
		{
			torchStatusBroadcast(context, value);
			setTorchROOT(value);
		}
		return value;
	}

	public static int setTorch(Context context, boolean increase) 
	{
		return setTorch(context, incrementedTorchValue(context, increase));
	}

	/*******************************************************************************
	 * Privates For Changing The Torch Brightness
	 *******************************************************************************/
	private static int incrementedTorchValue(Context context, boolean increase) 
	{
		int brightness = getCurrentBrightness(context);
		int maxBright = getBrightnessLimitValue(context);
		int increment = getIncrementSteps(context);
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
	
	
	private static boolean setTorchROOT(Shell shell, int value) 
	{
		String commandString = 
				"echo " + String.valueOf(value) + " > "+ getSysFsFile();
		
		CommandCapture command = new CommandCapture(0, commandString);
		
		try {
			Command c = shell.add(command);
			while (!c.isFinished()) 
			{
				Thread.sleep(1);
			}
			return true;
		} catch (Exception e) 
		{
			return false;
		}
	}

	
	private static boolean setTorchROOT(int value) 
	{
		try {
			setTorchROOT(RootTools.getShell(true), value);
			return true;
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return false;
	}

	
	/*******************************************************************************
	 * Root Access Methods and Compatibility Checking
	 *******************************************************************************/
	public static boolean checkCompatibility(Activity a) 
	{
		SharedPreferences settings =
				PreferenceManager.getDefaultSharedPreferences(a);
		
		if(getSysFsFile() != null) 
		{
			settings.edit()
				.putInt(SETTINGS_VALID_SYSFILE, 1)
				.commit();
			
			if(RootTools.isRootAvailable() & RootTools.isAccessGiven()) 
			{
				settings.edit()
					.putInt(SETTINGS_ROOT_ACCESS, 1)
					.commit();
				return true;
			} 
			else {
				settings.edit()
					.putInt(SETTINGS_ROOT_ACCESS, 0)
					.commit();
			}
		} 
		else {
			settings.edit()
				.putInt(SETTINGS_VALID_SYSFILE, 0)
				.commit();
		}
		return false;
	}

	
	public static boolean checkValidity(Context c, int value) 
	{
		if (value > AbsoluteMaxBrightness | value < 0) 	
		{	
			Toast.makeText(c, "UNSUPPORTED OR INVALID: \n-BRIGHTNESS LEVEL", Toast.LENGTH_SHORT).show();
			return false;	
		}
		SharedPreferences settings = 	PreferenceManager.getDefaultSharedPreferences(c);
		
		if(ignoreDeviceValidity) return ignoreDeviceValidity; //for android emulator
		
		if(settings.getInt(SETTINGS_VALID_SYSFILE, 0) == 1) 
		{
			if(settings.getInt(SETTINGS_ROOT_ACCESS, 0) == 1) 	
			{					
				return true;	
			}
			else {
				Toast.makeText(c, "UNSUPPORTED OR INVALID: \n-ROOT ACCESS", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			Toast.makeText(c, "UNSUPPORTED OR INVALID: \n-DEVICE", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	
	public static String getSysFsFile() 
	{
		if (FLASH_FILE != null)  return FLASH_FILE;
		
		for (String filePath : listOfFlashFiles) 
		{
			File flashFile = new File(filePath);
			if (flashFile.exists()) {	FLASH_FILE = filePath;	}
		}
		return FLASH_FILE;
	}

	
	
}



