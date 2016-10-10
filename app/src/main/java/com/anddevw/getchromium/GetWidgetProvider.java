package com.anddevw.getchromium;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

import static com.anddevw.getchromium.GetChromium.WIDGET_BUTTON;

public class GetWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        Log.i("ExampleWidget",  "Updating widgets " + Arrays.asList(appWidgetIds));

        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
//            Intent intent = new Intent(context, GetChromium.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            Intent intent = new Intent(WIDGET_BUTTON);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            views.setOnClickPendingIntent(R.id.button, pendingIntent);
//            // Get the layout for the App Widget and attach an on-click listener
//            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.button, pendingIntent);


            if (WIDGET_BUTTON.equals(intent.getAction())) {
                //Your Code
//                GetChromium.runSetup();
//                downloadLaapstChange();
            }


            // Tell the AppWidgetManager to perform an update on the current app
            // widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}