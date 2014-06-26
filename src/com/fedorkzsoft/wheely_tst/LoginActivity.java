package com.fedorkzsoft.wheely_tst;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
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
        }
        
        findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doLogin();
			}
		});
        
        initBroadcast();
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
				
				if (res == 0){// auth ok
					openMapVindow();
				} else if (res == WheelyService.NOT_AUTH){
					showAlert(getString(R.string.err_not_auth));
				} else {
					showAlert(getString(R.string.err_network));
				}
			}
		};
		
		IntentFilter intFilt = new IntentFilter(WheelyService.BROADCAST_ACTION_LOGIN);
        registerReceiver(mBroadcastReciever, intFilt);        
	}


    protected void openMapVindow() {
		// TODO Auto-generated method stub
		
	}


	protected void doLogin() {
    	String username = mUsernameEdt.getText().toString();
    	String password = mPasswordEdt.getText().toString();
    	if (verify(username, password)){
    		mPrefs.setLogin(username);
    		mPrefs.setPass(password);
    		
        	startWeelyService();
    	} else {
    		showAlert(getString(R.string.err_invalid_login));
    	}
	}


	private void startWeelyService() {
		Intent intent = new Intent(this, WheelyService.class);
		startService(intent);
	}


	private void showAlert(String string) {
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
