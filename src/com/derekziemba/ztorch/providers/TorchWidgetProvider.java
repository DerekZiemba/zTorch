package com.derekziemba.ztorch.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Tools;
import com.derekziemba.ztorch.activities.MainActivity;

public class TorchWidgetProvider extends AppWidgetProvider {
	public static final String WIDGET_TAG = "TorchWidget";
	private RemoteViews rView = null;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(WIDGET_TAG, "onUpdate");
		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			Intent openAppIntent = new Intent(context, MainActivity.class);
			openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			openAppIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent openIntent =PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			getRemoteView(context).setOnClickPendingIntent(R.id.button_increase, Tools.getPendingIntent(context, 1, appWidgetId));
			getRemoteView(context).setOnClickPendingIntent(R.id.button_decrease, Tools.getPendingIntent(context, -1, appWidgetId));
			getRemoteView(context).setOnClickPendingIntent(R.id.icon_button, openIntent);
			appWidgetManager.updateAppWidget(appWidgetId, getRemoteView(context));
		}
	}
	private RemoteViews getRemoteView(Context context) {
		if(rView == null) {	rView = new RemoteViews(context.getPackageName(), R.layout.small_widget_layout);	}
		return rView;
	}
}   