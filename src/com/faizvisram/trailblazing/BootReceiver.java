/**
 * 
 */
package com.faizvisram.trailblazing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Faiz
 *
 */
public class BootReceiver extends BroadcastReceiver {
	public final static int REQUEST_START_UPDATE_INTENT = 1000;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		updateAlarm(context);
	}
	
	/**
	 * Update the alarm for UpdateService to start now and fire every half hour.
	 * 
	 * @param context	Context to use to setup the alarm.
	 */
	public static void updateAlarm(Context context) {
		long firstTrigger = System.currentTimeMillis();

		// Setup the pending intent for UpdateService
		Intent intent = new Intent(context, UpdateService.class);
		PendingIntent pi = PendingIntent.getService(context, REQUEST_START_UPDATE_INTENT, 
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Schedule the update service
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC,
        		firstTrigger, AlarmManager.INTERVAL_HALF_HOUR, pi);
	}

}
