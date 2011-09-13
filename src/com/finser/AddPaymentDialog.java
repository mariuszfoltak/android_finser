package com.finser;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddPaymentDialog extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_payment_dialog);
        
        Button btnAdd = (Button) findViewById(R.id.widget_dodaj);
        btnAdd.setOnClickListener(addPaymentListener);
        
        Button btnCancel = (Button) findViewById(R.id.widget_anuluj);
        btnCancel.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
        		finish();
        	}
        });
        
        EditText et = (EditText) findViewById(R.id.widget_addpayment);
        
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE){
					addPayment();
				}
				return true;
			}
		});
    }
    
    @Override
	protected void onStart() {
        super.onStart();
        
        GoogleAnalyticsTracker tracker = ((FinserApplication)getApplicationContext())
        .getGoogleAnalyticsTracker();
        tracker.trackPageView("/add_payment_widget");
        tracker.dispatch();
	}
	
    private OnClickListener addPaymentListener = new OnClickListener(){
    	public void onClick(View v) {
    		addPayment();
    	}
    };
    
    private void addPayment() {
    	FinserApplication finserApp = (FinserApplication)getApplicationContext();

    	try {
    		FinserApi api = finserApp.getFinserApi();
    		EditText et = (EditText) findViewById(R.id.widget_addpayment);
    		switch (api.addPayment(et.getText().toString())) {
			case 200:
				Toast.makeText(getApplicationContext(), "Dodano operacjê", Toast.LENGTH_LONG).show();
				break;
			case 401:
				Toast.makeText(getApplicationContext(), R.string.no_payments, Toast.LENGTH_LONG).show();
				break;
			default:
				Toast.makeText(getApplicationContext(), "Sorry, nieznany b³¹d", Toast.LENGTH_LONG).show();
				break;
			}
    		et.setText("");
		} catch (ClientProtocolException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (ParseException e) {
			Log.e("Finser", e.getStackTrace().toString());
		} catch (IOException e) {
			Log.e("Finser", e.getStackTrace().toString());
		}
    }
}
