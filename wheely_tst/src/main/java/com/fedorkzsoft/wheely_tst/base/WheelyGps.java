	package com.fedorkzsoft.wheely_tst.base;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class WheelyGps implements LocationListener {
	private static final int MIN_DISTANCE = 2;
	private LocationManager mlocManager;
	private Location mCurLoc;
	
	private boolean mGpsReady;

	LocationListener mLoclistener;
	
	public WheelyGps(Context ctx) {
		mlocManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
	}

	public void start() {
		try {
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, MIN_DISTANCE, this);
			
			Location loc = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if (loc == null)
				loc = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			if (loc != null){// have got som location. Old but whatever...
				onLocationChanged(loc);
			}
			
//			mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE, this);
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, MIN_DISTANCE, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		if (mlocManager != null) {
			mlocManager.removeUpdates(this);
		}
	}
	
	public boolean isGpsEnabled() {
		return mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public boolean isNetworkEnabled() {
		return mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public void setLoclistener(LocationListener loclistener) {
		this.mLoclistener = loclistener;
	}
	
	public boolean hasLatLng() {
		return mCurLoc != null;
	}
	
	public void onLocationChanged(Location newLocation) {
		String provider = newLocation.getProvider();
		float accuracy = newLocation.getAccuracy();
		
		if ( provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER) ){
			mGpsReady = true;
		}
		
		if (mGpsReady && !provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)){
			return;
		}

		mCurLoc = newLocation;
			
		if (mLoclistener != null)
			mLoclistener.onLocationChanged(mCurLoc);
	}

	public void onProviderDisabled(String provider) {
		if (mLoclistener != null)
			mLoclistener.onProviderDisabled(provider);
	}

	public void onProviderEnabled(String provider) {
		if (mLoclistener != null)
			mLoclistener.onProviderDisabled(provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
