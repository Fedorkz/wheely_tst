package com.fedorkzsoft.wheely_tst.base;

import com.google.android.gms.maps.model.LatLng;

//no logic here, treat it as structure
public class Car {
	public int id;
	public LatLng loc;
	
	public Car() {
	}
	
	public Car(int _id, LatLng _loc) {
		id = _id;
		loc = _loc;
	}
	
	public Car(Car c) {
		id = c.id;
		loc = c.loc;
	}
}
