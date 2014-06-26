package com.fedorkzsoft.wheely_tst;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WheelyPreferences {
	private static final String PREF_LOGIN = "pref.login";
	private static final String DEFAULT_LOGIN = null;
	
	private static final String PREF_PASS = "pref.pass";
	private static final String DEFAULT_PASS = null;

	private SharedPreferences mPrefs;
	private Context mContext;
	
	public WheelyPreferences(Context context) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		mContext = context;
	}
	
	public String getLogin() {
		return mPrefs.getString(PREF_LOGIN, DEFAULT_LOGIN);
	}
	
	public void setLogin(String login) {
		SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_LOGIN, login);
		editor.commit();		
	}
	
	public String getPass() {
		return mPrefs.getString(PREF_PASS, DEFAULT_PASS);
	}
	
	public void setPass(String pass) {
		SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_PASS, pass);
		editor.commit();		
	}
	
}
