package com.derekziemba.ztorch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


public class Tools {
	

	public static enum Errors 
	{
		NO_SYSFS_FILE,
		NO_ROOT,
		NO_ROOT_ACCESS
	}

	public static void createDialog(	
			int titleId, 
			int messageId, 
			int positiveTextId, 
			DialogInterface.OnClickListener positiveAction,	
			int negativeTextId, 
			DialogInterface.OnClickListener negativeAction, 
			Context context	) 
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
		.setTitle(titleId)
		.setMessage(messageId);
		if (positiveTextId != 0 && positiveAction != null) { alertDialog.setPositiveButton(positiveTextId, positiveAction); }
		if (negativeTextId != 0 && negativeAction != null) { alertDialog.setNegativeButton(negativeTextId, negativeAction); }
		alertDialog.setCancelable(false);
		alertDialog.show();
	}
	
	
	public static void createSliderDialog(	
			int titleId, 
			final int messageId, 
			int max, 
			int positiveTextId, 
			DialogInterface.OnClickListener positiveAction, 
			final Context context	) 
	{
		final View content = 
				LayoutInflater.from(context)
				.inflate(R.layout.new_maximum_layout, null);
		final SeekBar seekBar = 
				(SeekBar)content.findViewById(R.id.seekbar);
		
		seekBar.setMax(max);
		seekBar.setOnSeekBarChangeListener(	
			new SeekBar.OnSeekBarChangeListener() {
				final TextView textView = (TextView)content.findViewById(R.id.instructions_textview);
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
				{
					textView.setText(context.getText(R.string.slider_instructions)
										.toString()
										.replace("REPLACE", String.valueOf(context.getText(messageId))) );
					
					textView.setText(context.getText(R.string.slider_instructions)
										.toString()
										.replace("%s", String.valueOf(progress)) );
				}
	
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { }
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { }			
	
			}
		);
		
		seekBar.setProgress(0);

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
			.setTitle(R.string.set_maximum)
			.setTitle(titleId)
			.setView(content);
		
		if (positiveTextId != 0 && positiveAction != null) 
		{ 
			alertDialog.setPositiveButton(positiveTextId, positiveAction); 
		}
		
		alertDialog.setNegativeButton(	R.string.cancel, 
			new DialogInterface.OnClickListener() {	
				@Override 
				public void onClick(DialogInterface dialog, int which) {	} 
			}
		);
		
		alertDialog.setCancelable(false);
		alertDialog.show();		
	}	

	
	
	
	public static PendingIntent getPendingIntent(Context context, int action, int appWidgetId) {
		Intent intent = new Intent(context, ResultsService.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(String.valueOf(action));
		if(appWidgetId != -1) {	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);	}
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}


	public static boolean createErrorDialog(final Activity context, Tools.Errors error) 
	{
		OnClickListener dismiss = new OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) { dialog.dismiss();	}
		};
		
		switch (error) 
		{
		
		case NO_SYSFS_FILE:
			if(!Global.debugmode) 
			{
				createDialog(	
					R.string.error, 				//titleID
					R.string.no_sysfs_file, 		//messageID
					0, 								//positiveTextId
					null,  							//positiveActionListener
					R.string.uninstall_and_quit, 	//negativeTextId
					new OnClickListener() {			//PositiveActionListener
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();
							Uri packageUri = Uri.parse(String.format("package:%s", Global.PACKAGE_NAME));
							Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
							context.startActivity(uninstallIntent);				
							context.finish();
						}
					},
					context	);						//Context
			}
			break;
			
		case NO_ROOT:
			createDialog(R.string.error, R.string.device_not_rooted, 0, null, R.string.ok, dismiss,context);
			break;
		case NO_ROOT_ACCESS:
			createDialog(R.string.error, R.string.root_access_not_given, 0, null, R.string.ok, dismiss,context);
			break;
		default:
			break;
		}
		return false;
	}



}
