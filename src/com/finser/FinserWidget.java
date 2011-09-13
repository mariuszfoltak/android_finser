package com.finser;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class FinserWidget extends AppWidgetProvider {
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            
         // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, AddPaymentDialog.class);
            PendingIntent pendingAddPayment = PendingIntent.getActivity(context, 0, intent, 0);
            
            intent = new Intent(context, MainTabActivity.class);
            PendingIntent pendingFinser = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.btn_add_payment, pendingAddPayment);
            views.setOnClickPendingIntent(R.id.btn_finser, pendingFinser);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
		}
    }
}
