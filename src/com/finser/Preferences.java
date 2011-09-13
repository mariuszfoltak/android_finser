package com.finser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}
	
	protected void onStop() {
		super.onStop();
		FinserApplication fApp = (FinserApplication) getApplicationContext();
		SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(getApplicationContext());
		
		try {
			FinserApi fApi = fApp.getFinserApi();
			fApi.changeAccount(settings.getString("login", ""),
					settings.getString("password", ""));
		} catch (ClientProtocolException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (UnsupportedEncodingException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (IOException e) {
			Log.e("Finser", e.getStackTrace().toString());
		}
		
	}
}
