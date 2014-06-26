package com.fedorkzsoft.wheely_tst;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

public class WheelyJson {
	private static final String KEY_LAT = "lat";
	private static final String KEY_LON = "lon";
	private static final String KEY_ID = "id";
	
	

	public static String SerializeLocation(LatLng loc){
		JSONObject jso = new JSONObject();
		try {
			jso.put(KEY_LAT, loc.latitude);
			jso.put(KEY_LON, loc.longitude);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return jso.toString();
	}
	
	public static Pair<Integer, LatLng> parseLocation(JSONObject jso){
		try {
			LatLng loc = new LatLng(
					jso.getDouble(KEY_LAT), 
					jso.getDouble(KEY_LON)
				);
			int id = jso.getInt(KEY_ID);

			return new Pair<Integer, LatLng>(id, loc);
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<Pair<Integer, LatLng>> parseLocationList(JSONArray arr){
		int n = arr.length();
		ArrayList<Pair<Integer, LatLng>> cars = new ArrayList<Pair<Integer, LatLng>>(n);
		
		for (int i=0; i<n; i++){
			try {
				Pair<Integer, LatLng> car = parseLocation(arr.getJSONObject(i));
				if (car != null){
					cars.add(car);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return cars;
	}
	
	
}
