package com.finser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FinserApplication extends Application {
	private FinserApi finserApi;
	private GoogleAnalyticsTracker tracker;
	private String LOGIN;
	private String PASS;
	
	public FinserApplication() {
		super.onCreate();
	}
	
	public GoogleAnalyticsTracker getGoogleAnalyticsTracker() {
		if(tracker==null) {
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.startNewSession("UA-25603738-1", this);
		}
		return tracker;
	}
	
	public FinserApi getFinserApi()
	throws ClientProtocolException, UnsupportedEncodingException, IOException {
		if(finserApi == null) {
			SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
	    	LOGIN = settings.getString("login", "");
	    	PASS  = settings.getString("password", "");
			finserApi = new FinserApi(LOGIN, PASS);
		}
		return finserApi;
	}
	
	public boolean checkPrefs()
	throws ClientProtocolException, UnsupportedEncodingException, IOException {
		SharedPreferences settings = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		
		if(!settings.contains("login")||!settings.contains("password"))
			return false;
		
		return(!settings.getString("login", "").equals("") &&
			   !settings.getString("password", "").equals(""));
	}
}
