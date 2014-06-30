package com.fedorkzsoft.wheely_tst;


import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.ImageView;

import com.fedorkzsoft.wheely_tst.base.Car;
import com.fedorkzsoft.wheely_tst.base.WheelyJson;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WheelyMapActivity extends FragmentActivity {
	public static final String TAG = "WheelyMap";

	private GoogleMap mMap;
	boolean mIsFollowingLocation = false;
	
	LatLng mLoc;
	
	private ImageView mMapCtrlFollowBtn;
	BroadcastReceiver mBroadcastReciever;
	
	ArrayList<Marker> mMarkers = new ArrayList<Marker>();
	
	SparseArray<Pair<Car, Marker>> mPersonMarkers = new SparseArray<Pair<Car,Marker>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		if ( setupMap() ){
			initBroadcast();
		} else {
        	AlertDialog ad = showError(R.string.err_nomapv2);
        	ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mBroadcastReciever != null)
			unregisterReceiver(mBroadcastReciever);

		super.onDestroy();
	}

	protected void onGotCarsList(final String str) {
		new AsyncTask<String, String, List<Car>>() {

			@Override
			protected List<Car> doInBackground(String... params) {
				return WheelyJson.parseCarList(str);
			}
			
			@Override
			protected void onPostExecute(List<Car> result) {
				if (result != null)
					mergePersonsToList(result);

				super.onPostExecute(result);
			}
			
		}.execute("");
	}

	private void mergePersonsToList(List<Car> lst) {
		if (lst == null)
			return;
		
		//merging result
		for (Car car: lst){
			int id = car.id;
			
			Pair<Car, Marker> pair = mPersonMarkers.get(id);
			if (pair != null){
				Marker marker = pair.second;
				
				mPersonMarkers.put(id, new Pair<Car, Marker>(car, marker));

				updateMarker(pair.first, pair.second);
			} else {
				Marker m = createMarker(car);
				if (m != null)
					mPersonMarkers.put(car.id, new Pair<Car, Marker>(car, m));
			}
		}
	}

	private Marker createMarker(Car p) {
		if (mMap == null)
			return null;
		
		return mMap.addMarker(new MarkerOptions()
			.title(""+p.id)
	//		.icon(new BitmapDescriptor())
			.snippet(p.id + " :: " +p.id)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
			.position(p.loc));	
		
	}

	private void updateMarker(Car p, Marker m) {
		m.setPosition(p.loc);
		m.setTitle("" + p.id);
	}

	protected void toggleFollow() {
		setFollow(!mIsFollowingLocation);
	}
	
	private void setFollow(boolean follow) {
		mIsFollowingLocation = follow;
		mMapCtrlFollowBtn.setImageLevel(mIsFollowingLocation ? 1 : 0);
	}
	
	
	///////////////////////////////////////////////////////////////////

	private boolean setupMap() {
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null){
	    	mMap.getUiSettings().setCompassEnabled(true);
	    	mMap.getUiSettings().setMyLocationButtonEnabled(true);
	    	mMap.setMyLocationEnabled(true);
	    	mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	    	return true;
        } else {
        	return false;
        }
    }
	
	private AlertDialog showError(int msg) {
		AlertDialog ad = 
		new AlertDialog.Builder(this)
		 	.setTitle(R.string.err)
			.setMessage(msg)
			.setPositiveButton(R.string.ok, null)
			.create();
		
		ad.show();
		return ad;
	}

	private void initBroadcast() {
		mBroadcastReciever = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent in) {
				String str = in.getStringExtra(WheelyService.POINTS_JSON);
				
				if (str != null){
					onGotCarsList(str);
				}
			}
		};
		
		IntentFilter intFilt = new IntentFilter(WheelyService.BROADCAST_ACTION_POINTS);
        registerReceiver(mBroadcastReciever, intFilt);        
	}
}