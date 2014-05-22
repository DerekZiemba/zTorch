package com.derekziemba.ztorch.providers;

import com.derekziemba.ztorch.Global;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Tools;
import com.derekziemba.ztorch.TorchConfig;

public class StaticTorchWidgetProvider extends AppWidgetProvider {
	public static final String WIDGET_TAG = "TorchWidget";
	private RemoteViews rView = null;
	
	private RemoteViews getRemoteView(Context context) {
		if(rView == null) {	rView = new RemoteViews(context.getPackageName(), R.layout.static_widget_layout);	}
		return rView;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) { //Update the icon on the static widget
		super.onReceive(context, intent);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);		
		if(appWidgetManager!=null) {
			int newValue = intent.getIntExtra(Global.KEY_NEW_VALUE, 0);		
			if(newValue==0) {
				getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_off);
			} else {
				getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_on);
			}
			int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, StaticTorchWidgetProvider.class));
			Log.i(WIDGET_TAG, "Hail! "+ids);
			
			for(int i=0; i<ids.length; i++){
				int appWidgetId = ids[i];
				getRemoteView(context).setOnClickPendingIntent(R.id.icon_button,Tools.getPendingIntent(context, 256, appWidgetId));
				appWidgetManager.updateAppWidget(ids[i], getRemoteView(context));
			}
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(WIDGET_TAG, "onUpdateStatic");
		final int N = appWidgetIds.length;
		int currentStatus = TorchConfig.getCurrentBrightness(context);	
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			if(currentStatus==0) {	getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_off);	} 
			else{	getRemoteView(context).setImageViewResource(R.id.icon_button, R.drawable.ic_flash_on);	}
			getRemoteView(context).setOnClickPendingIntent(R.id.icon_button, Tools.getPendingIntent(context, 256, appWidgetId));
			appWidgetManager.updateAppWidget(appWidgetId, getRemoteView(context));
		}
	}
}








