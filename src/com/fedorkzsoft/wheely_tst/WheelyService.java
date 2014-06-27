package com.fedorkzsoft.wheely_tst;

import java.util.ArrayList;

import com.fedorkzsoft.wheely_tst.base.WheelyGps;
import com.fedorkzsoft.wheely_tst.base.WheelyJson;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fedorkzsoft.wheely_tst.R;

import de.tavendo.autobahn.WebSocket.ConnectionHandler;

public class WheelyService extends Service {

	private static final int NOT_AUTH_SVR = 6;
	public static final String BROADCAST_ACTION_LOGIN = "brkst.login";
	public static final String AUTH_RESULT = "brkst.authresult";
	public static final int NOT_AUTH = 403;
	public static final int ALREADY_ALIVE = -2;
	
	public static final String BROADCAST_ACTION_POINTS = "brkst.points";
	public static final String POINTS_JSON = "brkst.pointsjson";
	
	public static final String ACTION_CHECK_ALIVE = "action.checkalive";
	public static final String ACTION_LOGIN = "action.login";
	

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
    private static boolean isConnected = false;
    
	private WheelyGps mGpsInfo;
	protected LatLng mCurLoc;

	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	
	private LatLngBounds mBounds;
	private int mLatestNotification = -1;
	
	WheelyPreferences mPrefs;
	
	WheelyWebSocket mWheelyWebSocket;

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
        isConnected = false;
    }
	   
    public static boolean isRunning()
    {
        return isConnected;
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
				sendLocationToWheely(mCurLoc);
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
    		sendLocationToWheely(mCurLoc);
    }

	private void sendLocationToWheely(LatLng loc) {
		mWheelyWebSocket.sendTextMessage(
				WheelyJson.SerializeLocation(loc)
			);
    }

	private void sendGpsEnabled() {
	}
	
	private void sendGpsDisabled() {
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String act = intent.getAction();
		if (ACTION_CHECK_ALIVE.equalsIgnoreCase(act) && isConnected){
			sendIsAlive();
		} else if (ACTION_LOGIN.equalsIgnoreCase(act)){
				if (!isConnected){
		        showNotification(NOTIFICATION_CONNECTION_STARTED);
		        setupWebSockets(mPrefs.getLogin(), mPrefs.getPass());
			} else {
				sendAuthResult(0);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
    public void onCreate() {
        super.onCreate();

        mPrefs = new WheelyPreferences(this);
        setupGPS();
    }
	
	private void setupWebSockets(String login, String pass) {
		mWheelyWebSocket = new WheelyWebSocket(login, pass);
		mWheelyWebSocket.connectToWheely(new ConnectionHandler() {
			
			@Override
			public void onTextMessage(String payload) {
				sendPoints(payload);
			}
			
			@Override
			public void onRawTextMessage(byte[] payload) {
			}
			
			@Override
			public void onOpen() {
				isConnected = true;				
				showNotification(NOTIFICATION_ONLINE);
				sendAuthResult(0);
				if (mCurLoc != null)
					sendLocationToWheely(mCurLoc);
			}
			
			@Override
			public void onClose(int code, String reason) {
				isConnected = false;				
				
				showNotification(NOTIFICATION_OFFLINE);
				if (code == NOT_AUTH_SVR){
					sendAuthResult(NOT_AUTH);
				}else{
					mWheelyWebSocket.reconnectToWheely();
				}
			}
			
			@Override
			public void onBinaryMessage(byte[] payload) {
				// TODO Auto-generated method stub
			}
		});
	}

	protected void sendIsAlive() {
		Intent in = new Intent(BROADCAST_ACTION_LOGIN);
		in.putExtra(AUTH_RESULT, ALREADY_ALIVE);
		sendBroadcast(in);
	}

	protected void sendAuthResult(int res) {
		Intent in = new Intent(BROADCAST_ACTION_LOGIN);
		in.putExtra(AUTH_RESULT, res);
		sendBroadcast(in);
	}
	
	protected void sendPoints(String res) {
		Intent in = new Intent(BROADCAST_ACTION_POINTS);
		in.putExtra(POINTS_JSON, res);
		sendBroadcast(in);
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
        
        
//        Notification notification = new Notification.Builder(this)
//	        .setContentTitle(message)
//	        .setContentText(message)
//	        .build();
        
// to support A2.2        
		Notification notification = new  Notification(
        		icon, 
        		message, 
        		System.currentTimeMillis());
        
        String title = getString(R.string.app_name);
        Intent notificationIntent = new Intent(this, WheelyMapActivity.class);
        
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, title, message, intent);
        
//        notification.flags = Notification.FLAG_ONGOING_EVENT;
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