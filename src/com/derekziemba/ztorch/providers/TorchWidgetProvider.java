package com.derekziemba.ztorch.providers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.derekziemba.torchplayer.Torch;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Z;
import com.derekziemba.ztorch.ZTorchWorker;


public class TorchWidgetProvider extends AppWidgetProvider {
	
	private RemoteViews rv = null;
	private RemoteViews getRemoteView(Context context) {
		if(rv == null) {	rv = new RemoteViews(context.getPackageName(), R.layout.static_widget_layout);	}
		return rv;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) { //Update the icon on the static widget
		super.onReceive(context, intent);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);		
		if(appWidgetManager!=null) {
			int newValue = intent.getIntExtra(Torch.CURRENT_BRIGHTNESS, 0);		
			if(newValue==0) {
				getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_off);
			} else {
				getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_on);
			}
			int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, TorchWidgetProvider.class));
			
			for(int i=0; i<ids.length; i++){
				int appWidgetId = ids[i];
				getRemoteView(context).setOnClickPendingIntent(R.id.icon_button, ZTorchWorker.getWidgetIntent(context, 256, appWidgetId));
				appWidgetManager.updateAppWidget(ids[i], getRemoteView(context));
			}
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		final int N = appWidgetIds.length;
		int currentStatus = Z.getInt(context,Torch.CURRENT_BRIGHTNESS);	
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			if(currentStatus==0) {	
				getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_off);	
			} 
			else {	
				getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_on);	
			}
			getRemoteView(context).setOnClickPendingIntent(R.id.icon_button, ZTorchWorker.getWidgetIntent(context, 256, appWidgetId));
			appWidgetManager.updateAppWidget(appWidgetId, getRemoteView(context));
		}
	}
}








