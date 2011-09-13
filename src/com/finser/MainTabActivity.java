package com.finser;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		// AddPayment Activity
		intent = new Intent().setClass(this, AddPaymentActivity.class);
		spec = tabHost.newTabSpec("addPayment")
					.setIndicator("Dodaj", res.getDrawable(R.drawable.ic_tab_addpayment))
					.setContent(intent);
		tabHost.addTab(spec);
		
		// Payments Tab
		intent = new Intent().setClass(this, PaymentsActivity.class);
		spec = tabHost.newTabSpec("payments")
					.setIndicator("P³atnoœci", res.getDrawable(R.drawable.ic_tab_payments))
					.setContent(intent);
		tabHost.addTab(spec);
		
		// Accounts Tab
		intent = new Intent().setClass(this, AccountsActivity.class);
		spec = tabHost.newTabSpec("accounts")
					.setIndicator("Konta", res.getDrawable(R.drawable.ic_tab_accounts))
					.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTabByTag("addPayment");
	}
}
