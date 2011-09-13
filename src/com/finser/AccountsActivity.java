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
import android.widget.Toast;

public class AccountsActivity extends ListActivity {
	private MergeAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts_list);
        
        adapter = new MergeAdapter();
        getAccounts();
        
        setListAdapter(adapter);
	}
	
	@Override
	protected void onStart() {
        super.onStart();
        
        GoogleAnalyticsTracker tracker = ((FinserApplication)getApplicationContext())
        .getGoogleAnalyticsTracker();
        tracker.trackPageView("/accounts");
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
            getAccounts();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
      
    private void getAccounts() {
    	FinserApplication finserApp = (FinserApplication)getApplicationContext();
    	  
    	try {
    		if(!finserApp.checkPrefs()) {
				Toast.makeText(getApplicationContext(), "Podaj dane do logowania", Toast.LENGTH_LONG).show();
				Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
	        	startActivity(settingsActivity);
			} else {
				FinserApi api = finserApp.getFinserApi();
	  			api.getAccounts(this, adapter);
	  			adapter.notifyDataSetChanged();
			}
  		} catch (ClientProtocolException e) {
  			Log.e("Finser", "getAccounts()", e);
  		} catch (ParseException e) {
  			Log.e("Finser", "getAccounts()", e);
  		} catch (IOException e) {
  			Log.e("Finser", "getAccounts()", e);
  		} catch (JSONException e) {
  			Log.e("Finser", "getAccounts()", e);
  		}
    }
}
