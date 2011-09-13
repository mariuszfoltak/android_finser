package com.finser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddPaymentActivity extends Activity {
	private Button btnDate;
	private Button btnTime;
	private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.add_payment);
        
        // Przypisanie akcji przyciskowi
        Button btnAdd = (Button) findViewById(R.id.btn_dodaj);
        btnAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addPayment();
			}});
        
        btnDate = (Button) findViewById(R.id.btnDate);
        btnDate.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}});
        
        btnTime = (Button) findViewById(R.id.btnTime);
        btnTime.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}});
        
        Button btnSetToNow = (Button) findViewById(R.id.btnSetToNow);
        btnSetToNow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setDateToNow();
			}});
        
        setDateToNow();
        
        EditText et = (EditText) findViewById(R.id.edit_payment);
        
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
        tracker.trackPageView("/add_payment");
        tracker.dispatch();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addpayment_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuPreferences:
        	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
        	startActivity(settingsActivity);
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	private void setDateToNow() {
		btnDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		btnTime.setText(new SimpleDateFormat("HH:mm").format(new Date()));
		
		final Calendar c = Calendar.getInstance();
        mYear 	= c.get(Calendar.YEAR);
        mMonth 	= c.get(Calendar.MONTH);
        mDay 	= c.get(Calendar.DAY_OF_MONTH);
        mHour 	= c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
	}
	
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mDateSetListener,
	                    mYear, mMonth, mDay);
	    case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                mTimeSetListener, mHour, mMinute, true);
	    }
	    return null;
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateDisplay();
            }
        };
    
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                updateDisplay();
            }
        };
	
	private void updateDisplay() {
		btnDate.setText(
            new StringBuilder()
        		.append(mYear).append("-")
                .append(String.format("%02d", mMonth + 1)).append("-")
                .append(String.format("%02d", mDay)));
		
		btnTime.setText(
	        new StringBuilder()
                .append(String.format("%02d", mHour)).append(":")
                .append(String.format("%02d", mMinute)));
    }
	
	private void addPayment() {
		FinserApplication finserApp = (FinserApplication)getApplicationContext();
		
		Time t = new Time();
		final Calendar c = Calendar.getInstance();
		t.timezone = c.getTimeZone().getDisplayName();
		t.set(0, mMinute, mHour, mDay, mMonth, mYear);
		
    	try {
    		if(!finserApp.checkPrefs()) {
				Toast.makeText(getApplicationContext(), "Podaj dane do logowania", Toast.LENGTH_LONG).show();
				Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
	        	startActivity(settingsActivity);
			} else {
	    		FinserApi api = finserApp.getFinserApi();
	    		EditText et = (EditText) findViewById(R.id.edit_payment);
	    		switch (api.addPayment(et.getText().toString(), t)) {
				case 200:
					Toast.makeText(getApplicationContext(), "Dodano operacjê", Toast.LENGTH_LONG).show();
					break;
				case 400:
					Toast.makeText(getApplicationContext(), "Niepoprawna sk³adnia", Toast.LENGTH_LONG).show();
					break;
				case 401:
					Toast.makeText(getApplicationContext(), "Aby dodaæ p³atnoœæ, musisz podaæ poprawne dane logowania", Toast.LENGTH_LONG).show();
					break;
				default:
					Toast.makeText(getApplicationContext(), "Sorry, nieznany b³¹d", Toast.LENGTH_LONG).show();
					break;
				}
    		et.setText("");
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
