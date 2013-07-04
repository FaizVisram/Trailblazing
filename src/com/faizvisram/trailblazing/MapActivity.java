package com.faizvisram.trailblazing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.IntentSender;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MapActivity extends Activity implements 
		GooglePlayServicesClient.ConnectionCallbacks, 
		GooglePlayServicesClient.OnConnectionFailedListener, OnMarkerClickListener {
	
	private static final int MAP_DEFAULT_ZOOM_LEVEL = 15;
	
	private LocationClient mLocationClient;
	private GoogleMap mMap;
	private ListView markerList;
	LocationAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		mLocationClient = new LocationClient(this, this, this);
		
		markerList = (ListView) findViewById(R.id.markerList);
		getActionBar().setTitle("My History");
	}

    @Override
    protected void onStart() {
        super.onStart();
        
        // Connect the location client
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the location client
        mLocationClient.disconnect();
        super.onStop();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        
    	// Setup the MapView inside the MapFragment
		setupMap();
		mAdapter = new LocationAdapter(this, R.layout.location_view, mMap);
		List<Location> history = getLocationHistory();
		mAdapter.addAll(history);
		markerList.setAdapter(mAdapter);
		BootReceiver.updateAlarm(this);
    }
    
    private void setupMap() {
    	mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMarkerClickListener(this);
		
	}

    public void getLocation() {
    	Location location = mLocationClient.getLastLocation();
    	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), 
        		location.getLongitude()), MAP_DEFAULT_ZOOM_LEVEL));
        mAdapter.add(location);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
    
    /**
     * Show Google Play Services licensing as per Google's requirement listed here:
     * https://developers.google.com/maps/documentation/android/intro#attribution_requirements
     *  
     * @param item	MenuItem that called this function.
     */
    public void showAbout(MenuItem item) {
    	String license = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
    	
    	AlertDialog aboutDialog = new AlertDialog.Builder(this)
    			.setCancelable(true)
    			.setTitle(getString(R.string.about))
    			.setInverseBackgroundForced(true)
    			.setMessage(license)
    			.setNeutralButton(getString(R.string.dismiss), null)
    			.create();
    	
    	aboutDialog.show();

    }

    /**
     * Show a popup message with the distance traveled today.
     * 
     * @param item	The MenuItem that called this function.
     */
    public void showMyDistance(MenuItem item) {
    	String message = String.format(getString(R.string.my_distance_today), mAdapter.getTodaysDistance());
    	
    	AlertDialog aboutDialog = new AlertDialog.Builder(this)
    			.setCancelable(true)
    			.setTitle(getString(R.string.about))
    			.setInverseBackgroundForced(false)
    			.setMessage(message)
    			.setNeutralButton(getString(R.string.dismiss), null)
    			.create();
    	
    	aboutDialog.show();

    }
    
    private String readFile(String filename) {
    	String contents = "";
    	File file = new File(getFilesDir() + "/" + filename);
    	BufferedReader reader;
    	
    	if (!file.exists())
    		return null;
    	
    	try {
			reader = new BufferedReader(new InputStreamReader(openFileInput(filename)));
	    	String line = null;

	    	while ((line = reader.readLine()) != null) {
				contents += line + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return contents;
    }
    
    private List<Location> getLocationHistory() {
    	final String provider = "fused";
    	
    	List<Location> locations = new ArrayList<Location>();
    	String file = readFile(UpdateService.FILE_LOCATION_HISTORY);
    	String[] entries;
    	
    	if (file == null || file.isEmpty())
    		return null;
    	
    	entries = file.trim().split("\n");
    	    	
    	for (int i = 0; i < entries.length; i++) {
    		String[] entry = entries[i].split(", ");
    		
    		if (entry.length == 3) {
    			Location location = new Location(provider);
    			
    			try {
	    			location.setTime(Double.valueOf(entry[0]).longValue());
	    			location.setLatitude(Double.parseDouble(entry[1]));
	    			location.setLongitude(Double.parseDouble(entry[2]));
	    			locations.add(location);
	    		} catch (NumberFormatException e) {
	    			e.printStackTrace();
	    		}
    		}
    	}
    	
    	return locations;
	}
    
    public void showBatteryStats(MenuItem item) {
    	String message = readFile(UpdateService.FILE_BATTERY_HISTORY);

    	if (message == null || message.isEmpty())
    		message = getString(R.string.battery_stats_unavailable);
    	
    	AlertDialog aboutDialog = new AlertDialog.Builder(this)
    			.setCancelable(true)
    			.setTitle(getString(R.string.battery_stats))
    			.setInverseBackgroundForced(true)
    			.setMessage(message)
    			.setNeutralButton(getString(R.string.dismiss), null)
    			.create();
    	
    	aboutDialog.show();

    }

	@Override
    public void onConnected(Bundle dataBundle) {
        getLocation();
    }
    
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
       if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        UpdateService.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Log.d("MapActivity", "Location Client Not Connected.");
        }
    }
    
    /**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

	@Override
	public boolean onMarkerClick(Marker marker) {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			Location l = mAdapter.getItem(i);
			if (marker.getPosition().equals(new LatLng(l.getLatitude(), l.getLongitude()))) {
				markerList.setSelection(i);
			}
		}
				
		return false; // Do default behaviour
	}
    
}
