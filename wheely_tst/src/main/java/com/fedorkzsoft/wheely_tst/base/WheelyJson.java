package com.fedorkzsoft.wheely_tst.base;

import java.util.ArrayList;
import java.util.Collection;

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
	
	public static Car parseCar(JSONObject jso){
		try {
			LatLng loc = new LatLng(
					jso.getDouble(KEY_LAT), 
					jso.getDouble(KEY_LON)
				);
			int id = jso.getInt(KEY_ID);
			
			return new Car(id, loc);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<Car> parseCarList(String str){
		
		JSONArray arr;

		try {
			arr = new JSONArray(str);
		} catch (JSONException e1) {
			e1.printStackTrace();// SMTH is broken, no need to continue
			return null;
		}
		
		int n = arr.length();
		ArrayList<Car> cars = new ArrayList<Car>(n);

		for (int i=0; i<n; i++){ 
			try {
				Car car = parseCar(arr.getJSONObject(i));
				if (car != null){
					cars.add(car);
				}
			} catch (JSONException e) {
				e.printStackTrace();// one car is broken, try to read others
			}
		}

		return cars;
	}
	
	
}
