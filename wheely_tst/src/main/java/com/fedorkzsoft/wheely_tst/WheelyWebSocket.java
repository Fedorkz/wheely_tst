package com.fedorkzsoft.wheely_tst;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.util.Log;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketOptions;

public class WheelyWebSocket extends WebSocketConnection{
	private static final String WHEELY_WEBSOCKET_URL = "ws://mini-mdt.wheely.com/?username=%s&password=%s";
	private static final String TAG = "WheelyWebSocket";
//	private WebSocketConnection sess = new WebSocketConnection();
	private String mLogin;
	private String mPass;
	private ConnectionHandler mConnectionHandler;
	
	public WheelyWebSocket(String login, String pass) {
		super();
		mLogin = login;
		mPass = pass;
	}
	
	public void connectToWheely(ConnectionHandler connectionHandler) {
		mConnectionHandler = connectionHandler;
		WebSocketOptions options = new WebSocketOptions();
//	      options.setVerifyCertificateAuthority(false);
		List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
		headers.add(new BasicNameValuePair("username", mLogin));
		headers.add(new BasicNameValuePair("password", mPass));
	      
		try {
			connect(getServerUrl(), null, connectionHandler, options, headers);

		} catch (WebSocketException e) {
			e.printStackTrace();
			Log.d(TAG, e.toString());
		}	
	}

	private String getServerUrl(){
		return String.format(WHEELY_WEBSOCKET_URL, mLogin, mPass);
	}

	public void reconnectToWheely() {
		connectToWheely(mConnectionHandler);
	}	
	
}
