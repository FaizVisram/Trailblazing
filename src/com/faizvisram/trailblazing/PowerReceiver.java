/**
 * 
 */
package com.faizvisram.trailblazing;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Handles changes to power status to preserve battery.
 * 
 * @author Faiz
 */
public class PowerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Power disconnected, clear battery history to restart stats
		if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
			File file = new File(context.getFilesDir() + "/" + UpdateService.FILE_BATTERY_HISTORY);
			if (file.exists())
				file.delete();
		}
	}

}
