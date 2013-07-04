package com.faizvisram.trailblazing;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Faiz
 *
 */
public class UpdateService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	protected static final String FILE_LOCATION_HISTORY = "loc.log";
	protected static final String FILE_BATTERY_HISTORY = "battery.log";
	protected static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 10000;
	
	private LocationClient mLocationClient;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient(this, this, this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		checkBattery();

		if (playServicesAvailable()) {
			if (mLocationClient.isConnected()) {
				getLocation();
			} else {
				mLocationClient.connect();
			}
			
			return START_STICKY; // make sticky to prevent service from ending before
		} else {
			return START_NOT_STICKY;
		}
	}
	
	private void getLocation() {
		Location location;
		
		if (mLocationClient.isConnected()) {
			location = mLocationClient.getLastLocation();
		
			saveLocation(this, location);
			stopSelf(); // location is saved, time to stop
		}
	}
	
	private void checkBattery() {
		float life = getBatteryPercentage();
		saveBattery(life);
	}
	
	private float getBatteryPercentage() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, filter);
		
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float percentage = level / (float) scale;
		
		return percentage;
	}
	
	private boolean playServicesAvailable() {
		int playServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	
		return ConnectionResult.SUCCESS == playServicesAvailable;
	}

	protected static void saveLocation(Context context, Location location) {
		FileOutputStream outputStream;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double time = location.getTime();
        String entry = time + ", " + latitude + ", " + longitude + "\n";
		
		try {
			outputStream = context.openFileOutput(FILE_LOCATION_HISTORY, Context.MODE_APPEND);
			outputStream.write(entry.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void saveBattery(float percentage) {
		Calendar now = Calendar.getInstance();
		String line = String.format(getString(R.string.battery_log_record), 
				new SimpleDateFormat("h:mm a").format(now.getTime()), percentage * 100) + "\n";
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(FILE_BATTERY_HISTORY, Context.MODE_APPEND);
			outputStream.write(line.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void onConnected(Bundle dataBundle) {
        getLocation();
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		// Cannot resolve connection issues in the background
    			// Stop service
    	Log.d("Google Play Services", connectionResult.toString());
    	stopSelf();
    }
    
    @Override
    public void onDisconnected() {
		// Cannot resolve connection issues in the background
				// Stop service
    	Log.d("Google Play Location Services", "Google Play Services Client disconnected.");
    	stopSelf();
    }
}
