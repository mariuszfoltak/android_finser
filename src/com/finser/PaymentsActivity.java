package com.finser;

import java.io.IOException;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentsActivity extends ListActivity {
	private MergeAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.payments_list);
        
        adapter = new MergeAdapter();
        getPayments();
        
        setListAdapter(adapter);
    }
    
    @Override
	protected void onStart() {
        super.onStart();
        
        GoogleAnalyticsTracker tracker = ((FinserApplication)getApplicationContext())
        .getGoogleAnalyticsTracker();
        tracker.trackPageView("/payments");
        tracker.dispatch();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuPreferences:
        	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
        	startActivity(settingsActivity);
        	return super.onOptionsItemSelected(item);
        case R.id.menuRefresh:
            getPayments();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void getPayments() {
    	getPayments("!last");
    }
    
    public void getPayments(String query) {
    	FinserApplication finserApp = (FinserApplication)getApplicationContext();

    	try {
    		if(!finserApp.checkPrefs()) {
				Toast.makeText(getApplicationContext(), "Podaj dane do logowania", Toast.LENGTH_LONG).show();
				Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
	        	startActivity(settingsActivity);
			} else {
	    		FinserApi api = finserApp.getFinserApi();
				api.getPayments(this, adapter, query);
				adapter.notifyDataSetChanged();
			}
		} catch (ClientProtocolException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (ParseException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (IOException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (JSONException e) {
			Log.e("Finser", e.getStackTrace().toString());
		}
    }
    
    public void myClickHandler(View v) 
    {
    	TextView txt = (TextView)v;
    	FinserApplication finserApp = (FinserApplication)getApplicationContext();

    	try {
    		FinserApi api = finserApp.getFinserApi();
			api.getPayments(this, adapter, txt.getText().toString());
			adapter.notifyDataSetChanged();
			
		} catch (ClientProtocolException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (ParseException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (IOException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (JSONException e) {
			Log.e("Finser", e.getStackTrace().toString());
		}     
    }

}