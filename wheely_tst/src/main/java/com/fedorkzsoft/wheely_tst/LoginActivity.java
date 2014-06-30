package com.fedorkzsoft.wheely_tst;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.os.Build;

public class LoginActivity extends ActionBarActivity {

	WheelyPreferences mPrefs;
	private EditText mUsernameEdt;
	private EditText mPasswordEdt;
	
	BroadcastReceiver mBroadcastReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mPrefs = new WheelyPreferences(this);
        
        mUsernameEdt = (EditText)findViewById(R.id.username);
        mPasswordEdt = (EditText)findViewById(R.id.password);
        
        String oldLogin = mPrefs.getLogin();
        if (oldLogin != null){
        	mUsernameEdt.setText(oldLogin);
        	mPasswordEdt.setText(mPrefs.getPass());// <-- Maybe remove this
        }
        
        findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doLogin();
//				openMapVindow();
			}
		});
        
        initBroadcast();
        queryCheckWeelyService();
    }
    
    @Override
    protected void onDestroy() {
    	unregisterReceiver(mBroadcastReciever);
    	super.onDestroy();
    }


	private void initBroadcast() {
		mBroadcastReciever = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent in) {
				int res = in.getIntExtra(WheelyService.AUTH_RESULT, -1);
				
				if (res == 0 || res == WheelyService.ALREADY_ALIVE){// auth ok
					finish();
					openMapVindow();
				} else if (res == WheelyService.NOT_AUTH){
					showAlert(R.string.err_not_auth);
					stopWeelyService();
				} else {
					showAlert(R.string.err_network);
					stopWeelyService();
				}
			}
		};
		
		IntentFilter intFilt = new IntentFilter(WheelyService.BROADCAST_ACTION_LOGIN);
        registerReceiver(mBroadcastReciever, intFilt);        
	}


    protected void openMapVindow() {
		 Intent intent = new Intent(this, WheelyMapActivity.class);
		 startActivity( intent );
	}


	protected void doLogin() {
    	String username = mUsernameEdt.getText().toString();
    	String password = mPasswordEdt.getText().toString();
    	if (verify(username, password)){
    		mPrefs.setLogin(username);
    		mPrefs.setPass(password);
    		
//    		stopWeelyService();
        	startWeelyService();
    	} else {
    		showAlert(R.string.err_invalid_login);
    	}
	}


	private void startWeelyService() {
		Intent intent = new Intent(this, WheelyService.class);
		intent.setAction(WheelyService.ACTION_LOGIN);
		startService(intent);
	}	
	

	private void queryCheckWeelyService() {
		Intent intent = new Intent(this, WheelyService.class);
		intent.setAction(WheelyService.ACTION_CHECK_ALIVE);
//		intent.putExtra(name, value)
		startService(intent);
	}		
	
	private void stopWeelyService() {
		Intent intent = new Intent(this, WheelyService.class);
		stopService(intent);
	}


	private void showAlert(int msg) {
		new AlertDialog.Builder(this)
		 	.setTitle(R.string.err)
			.setMessage(msg)
			.setPositiveButton(R.string.ok, null)
			.show();
	}

	private boolean verify(String username, String password) {
		return (username.length() > 0 && password.length() > 0 );
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
