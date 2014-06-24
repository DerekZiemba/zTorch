package com.derekziemba.ztorch.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.ZTorchWorker;

import com.derekziemba.ztorch.activities.MainActivity;

public class TwoBy1WidgetProvider extends AppWidgetProvider {
	private RemoteViews rv = null;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			Intent openAppIntent = new Intent(context, MainActivity.class);
			openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			openAppIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent openIntent =PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			if(rv == null) {	
				rv = new RemoteViews(context.getPackageName(), R.layout.small_widget_layout);	
			}

			rv.setOnClickPendingIntent(R.id.button_increase, ZTorchWorker.getWidgetIntent(context, 1, appWidgetId));
			rv.setOnClickPendingIntent(R.id.button_decrease, ZTorchWorker.getWidgetIntent(context, -1, appWidgetId));
			rv.setOnClickPendingIntent(R.id.icon_button, openIntent);
			appWidgetManager.updateAppWidget(appWidgetId, rv);
		}
	}
	
}   