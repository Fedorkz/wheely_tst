package com.fedorkzsoft.wheely_tst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.fedorkzsoft.wheely_tst.base.WheelyGps;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;

import com.fedorkzsoft.wheely_tst.R;

public class DiverseService extends Service {

	public static final int NOTIFICATION_ONLINE = 0;
	public static final int NOTIFICATION_OFFLINE = 1;
	public static final int NOTIFICATION_CONNECTION_STARTED = 2;
	
    private static final long UPDATE_PERIOD = 1000L;

	private static final int NOTIFICATION_ID = 1;
	public static final int MSG_REGISTER_CLIENT = 2;
	public static final int MSG_UNREGISTER_CLIENT = 3;
	private static final String MSG_LOCATION = "msg_loc";
	private static final int MSG_LOCATION_CHANGE = 5;
    
    private NotificationManager nm;
    private static boolean isRunning = false;
    
	private WheelyGps mGpsInfo;
	protected LatLng mCurLoc;

	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	private String mLogin;
	private String mPass;
	
	private LatLngBounds mBounds;
	private int mLatestNotification = -1;
	
	WheelyPreferences mPrefs;

	@Override
	public IBinder onBind(Intent intent) {
    	Log.i("svr", "SERVISE       BIND.");
		return mMessenger.getBinder();
	}

   @Override
    public void onDestroy() {
        super.onDestroy();
        
        nm.cancel(0); // Cancel the persistent notification.
        Log.i("svr", "SERVICE    STOP.");
        isRunning = false;
    }
	   
    public static boolean isRunning()
    {
        return isRunning;
    }	   
    
	private void setupGPS() {
		mGpsInfo = new WheelyGps(this);
    
		mGpsInfo.setLoclistener(new LocationListener() {
			@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override public void onProviderEnabled(String provider) {
				if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)){
					Log.i("GPS","sendGpsEnabled()");
					sendGpsEnabled();
				} else if (!mGpsInfo.isGpsEnabled() && provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)){
					Log.i("GPS","sendNetLocEnabled()");
				}
				
			}
			@Override public void onProviderDisabled(String provider) {
				Log.i("GPS","DISAB Prov: " + provider + "");
				if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)){
					Log.i("GPS","sendGpsDisabled()");
					sendGpsDisabled();
				} else if (!mGpsInfo.isGpsEnabled() && provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)){
					Log.i("GPS","sendNetLocEnabled()");
				}
			}
			
			@Override
			public void onLocationChanged(Location l) {
				Log.i("GPS","onLocationChanged");
				mCurLoc = new LatLng(l.getLatitude(), l.getLongitude());
				sendLocationToGame(mCurLoc);
//				GameState.setMyLocation(mCurLoc);
			}
		});
		
		mGpsInfo.start();
    
        if (!mGpsInfo.isGpsEnabled()) {
        	requestGpsDialog();
        }
	}

	private void requestGpsDialog() {
	}
	
	
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                sendKnownInfo();
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
                
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    public void sendKnownInfo() {
    	if (mCurLoc != null)
    		sendLocationToGame(mCurLoc);
//    	if (AppState.getPlayers().size() > 1 )
//    		sendUpdateUI();
    }

	private void sendLocationToGame(LatLng loc) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Bundle b = new Bundle();
                b.putParcelable(MSG_LOCATION, loc);
                Message msg = Message.obtain(null, MSG_LOCATION_CHANGE);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
//	private void sendConnectionLost() {
//		sendEmptyMessage(MSG_CONNECION_LOST);
//    }
//	
	private void sendGpsEnabled() {
//		sendEmptyMessage(MSG_GPS_ENABLED);
	}
	
	private void sendGpsDisabled() {
//        Bundle b = new Bundle();
//        b.putBoolean(MSG_GPS_NETWORK_ENABLED, networkEnabled);
//		
//		sendBundleMessage(MSG_GPS_DISABLED, b);
	}
	

	private void sendEmptyMessage(int msgId) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Message msg = Message.obtain(null, msgId);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
	

	private void sendBundleMessage(int msgId, Bundle b) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Message msg = Message.obtain(null, msgId);
                msg.setData(new Bundle(b));//create copy
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
    
	@Override
    public void onCreate() {
        super.onCreate();

        mPrefs = new WheelyPreferences(this);
        
        showNotification(NOTIFICATION_CONNECTION_STARTED);
        setupGPS();
        
        setupWebSockets(mPrefs.getLogin(), mPrefs.getPass());

        isRunning = true;
    }
	
	private void setupWebSockets(String login, String pass) {
	}

	@SuppressWarnings("deprecation")
	private void showNotification(int type) {
		
		if (mLatestNotification == type){
			return;
		}

		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		int icon = 0;
        String message = "";
        
        switch (type) {
        
        case NOTIFICATION_ONLINE:
        	
//        	icon = R.drawable.ic_status_online;
        	message = getString(R.string.status_online);
        	break;
        	
        case NOTIFICATION_OFFLINE:
        	
//        	icon = R.drawable.ic_status_offline;
        	message = getString(R.string.status_offline);
        	break;
        	
        case NOTIFICATION_CONNECTION_STARTED:
        	
//        	icon = R.drawable.ic_status_offline;
        	message = getString(R.string.status_connecting);
        	break;
        }
        
		Notification notification = new  Notification(
        		icon, 
        		message, 
        		System.currentTimeMillis());
        
        String title = getString(R.string.app_name);
        Intent notificationIntent = new Intent(this, LoginActivity.class);
        
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, title, message, intent);
        
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.defaults |= Notification.DEFAULT_SOUND;
        
//        nm.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
        mLatestNotification = type;
    }
    
	public static boolean checkNetwork(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        
        return (netInfo == null) ? false : true;
	}
  
}