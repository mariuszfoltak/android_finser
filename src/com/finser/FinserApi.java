package com.finser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.merge.MergeAdapter;

import android.app.Activity;
import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FinserApi {
	private HttpClient httpClient;
	
	public FinserApi(String login, String password)
	throws ClientProtocolException, UnsupportedEncodingException, IOException {
		httpClient = new DefaultHttpClient();
		logIn(login, password);
	};
	
	private HttpPost getHttpPost(String string) {
		HttpPost httpPost = new HttpPost(string);
		httpPost.addHeader("X-Finser-API", "0.5");
		httpPost.addHeader("User-Agent", "Android Finser");
		
		return httpPost;
	}
	
	private void logOut() throws ClientProtocolException, IOException {
		HttpPost httpPost = getHttpPost("http://api.finser.pl/logout/");
		httpClient.execute(httpPost);
	}
	
	public boolean changeAccount(String login, String password)
	throws ClientProtocolException, UnsupportedEncodingException, IOException {
		logOut();
		return logIn(login, password);
	}
	
	public boolean logIn(String login, String password) 
	throws ClientProtocolException, UnsupportedEncodingException, IOException
	{
		HttpPost httpPost = getHttpPost("http://api.finser.pl/login/");
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("username", login));
        formparams.add(new BasicNameValuePair("password", password));
        
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		httpPost.setEntity(entity);        
        
        HttpResponse response = httpClient.execute(httpPost);
        Log.d("Finser", Integer.toString(response.getStatusLine().getStatusCode()));
        return (response.getStatusLine().getStatusCode()==200);
	}
	
	public void getPayments(Context context, MergeAdapter adapter) 
	throws ClientProtocolException, IOException, ParseException, JSONException {
		getPayments(context, adapter, "!last"); 
	}
	
	public void getPayments(Context context, MergeAdapter adapter, String query) 
	throws ClientProtocolException, IOException, ParseException, JSONException
	{
		HttpPost httpPost = getHttpPost("http://api.finser.pl/get/");
		
    	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    	formparams.add(new BasicNameValuePair("query", query));
    	Log.d("Finser", query);
    	
    	UrlEncodedFormEntity params = new UrlEncodedFormEntity(formparams, "UTF-8");
    	httpPost.setEntity(params);
    	
    	HttpResponse response = httpClient.execute(httpPost);
    	
        switch(response.getStatusLine().getStatusCode()) {
        case 200:
        	convertPayments(context, adapter, EntityUtils.toString(response.getEntity()));
        }
	}
	
	public void getAccounts(Context context, MergeAdapter adapter)
	throws ClientProtocolException, IOException, ParseException, JSONException {
		HttpPost httpPost = getHttpPost("http://api.finser.pl/accounts/");
		HttpResponse response = httpClient.execute(httpPost);

        if(response.getStatusLine().getStatusCode()==200)
        	convertAccounts(context, adapter, EntityUtils.toString(response.getEntity()));
	}
	
	public int addPayment(String query)
	throws ClientProtocolException, IOException {
		Time t = new Time();
		t.setToNow();
		return addPayment(query, t);
	}
	
	public int addPayment(String query, Time t) 
	throws ClientProtocolException, IOException {
		HttpPost httpPost = getHttpPost("http://api.finser.pl/insert/");
		String sTime = String.valueOf((long)t.toMillis(false)/1000+t.gmtoff);
		
		if(query.equals(""))
			return 400;
		
    	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    	formparams.add(new BasicNameValuePair("text", query));
    	formparams.add(new BasicNameValuePair("time", sTime));
    	Log.d("Finser", sTime);
    	UrlEncodedFormEntity params = new UrlEncodedFormEntity(formparams, "UTF-8");
		httpPost.setEntity(params);
        
        return httpClient.execute(httpPost).getStatusLine().getStatusCode();
	}

	private void convertPayments(Context context, MergeAdapter adapter, String data) 
	throws JSONException {
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> item;
		JSONObject detail;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		Timestamp tm;
		TextView tv;
		Time t = new Time();
		
		t.setToNow();
		adapter.clear();
		
		JSONObject 	details = new JSONObject(data).getJSONObject("operations");
		JSONArray 	names	= details.names();
		
		for (int i = 0; i < names.length(); i++) {
			item = new HashMap<String,String>();
			detail = details.getJSONObject(names.getString(i));
			
			item.put("text", detail.getString("text"));
			item.put("timestamp", detail.getString("time"));
			if(detail.getString("type").equals("m"))
				item.put("account",
						detail.getString("account_from").toUpperCase()
						+" > "+detail.getString("account_to").toUpperCase());
			else
				item.put("account", detail.getString("account").toUpperCase());
			if(detail.getString("type").equals("t"))
				item.put("value", 
						detail.getString("value_from").replace(',', ' ').replace('.', ',')+
						" > "+detail.getString("value_to").replace(',', ' ').replace('.', ','));
			else if(detail.has("value"))
		  		item.put("value", detail.getString("value").replace(',', ' ').replace('.', ','));
		  	
			tm = new Timestamp((detail.getLong("time")-t.gmtoff)*1000);
			
		    item.put("date", dateFormat.format(tm));
		    //item.put("time", timeFormat.format(tm));
		    
		    list.add(item);
		}
		
		// Sortowanie od najm³odszego
		Collections.sort(list, new Comparator<HashMap<String,String>>() {
			public int compare(HashMap<String, String> object1,
					HashMap<String, String> object2) {
						return Integer.parseInt(object2.get("timestamp")) 
						- Integer.parseInt(object1.get("timestamp"));
			}
		});
		
		String old = "";
		ArrayList<HashMap<String,String>> list2 = new ArrayList<HashMap<String,String>>();
		for (HashMap<String, String> hashMap : list) {
			if(!hashMap.get("date").equals(old) && !list2.isEmpty()) {
					tv = (TextView) ((Activity) context).getLayoutInflater().
									inflate(R.layout.list_header, null);
					tv.setText(old);
					adapter.addView(tv);
					adapter.addAdapter(new SimpleAdapter(
				    		context, 
				    		list2, 
				    		R.layout.payment_item, 
				    		new String[] {"account","text","value", "time"}, 
				    		new int[] {R.id.txt_account, R.id.txt_text, R.id.txt_value, R.id.txt_time}));
				
				list2 = new ArrayList<HashMap<String,String>>();
			}
			
			list2.add(hashMap);
			old = hashMap.get("date");
		}
		
		if(!list2.isEmpty()) {
			tv = (TextView) ((Activity) context).getLayoutInflater().
			inflate(R.layout.list_header, null);
			tv.setText(old);
			adapter.addView(tv);
			adapter.addAdapter(new SimpleAdapter(
		    		context, 
		    		list2, 
		    		R.layout.payment_item, 
		    		new String[] {"account","text","value", "time"}, 
		    		new int[] {R.id.txt_account, R.id.txt_text, R.id.txt_value, R.id.txt_time}));
		}
	}
	
	private void convertAccounts(Context context, MergeAdapter adapter, String data)
	throws JSONException {
		ArrayList<HashMap<String,String>> list, summary = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> item;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2); //dla df ustawiamy najwiêksz¹ iloœæ miejsc po przecinku
		df.setMinimumFractionDigits(2); //dla df ustawiamy najmniejsz¹ iloœæ miejsc po przecinku
		TextView tv;
		
		double pln = 0, usd = 0, eur = 0, chf = 0, gbp = 0;
		
		adapter.clear();
		
		tv = (TextView) ((Activity) context).getLayoutInflater().
		inflate(R.layout.list_header, null);
		tv.setText("Podsumowanie:");
		adapter.addView(tv);
		adapter.addAdapter(new SimpleAdapter(
	    		context, 
	    		summary, 
	    		R.layout.account_item, 
	    		new String[] {"currency","value"}, 
	    		new int[] {R.id.txt_currency, R.id.txt_value}));
		
		JSONObject payments = new JSONObject(data);
		JSONArray names	= payments.names();
		
		for (int i = 0; i < names.length(); i++) {
			list = new ArrayList<HashMap<String,String>>();
			
			JSONObject payment = payments.getJSONObject(names.getString(i));
			try {
				JSONObject currencies = payment.getJSONObject("summary");
			
				JSONArray cNames = currencies.names();
				tv = (TextView) ((Activity) context).getLayoutInflater().
				inflate(R.layout.list_header, null);
				tv.setText(payment.getString("name"));
				adapter.addView(tv);
				
				for(int j=0; j < currencies.length(); j++) {
					JSONObject currency = currencies.getJSONObject(cNames.getString(j));
					item = new HashMap<String,String>();
					item.put("currency", cNames.getString(j));
					item.put("value", 
							df.format(currency.getDouble("plus")-currency.getDouble("minus")));
					
					String cur = cNames.getString(j);
					if(cur.equals("PLN"))
						pln += currency.getDouble("plus")-currency.getDouble("minus");
					else if(cur.equals("EUR"))
						eur += currency.getDouble("plus")-currency.getDouble("minus");
					else if(cur.equals("USD"))
						usd += currency.getDouble("plus")-currency.getDouble("minus");
					else if(cur.equals("GBP"))
						gbp += currency.getDouble("plus")-currency.getDouble("minus");
					else if(cur.equals("CHF"))
						chf += currency.getDouble("plus")-currency.getDouble("minus");
					
					list.add(item);
				}
			} catch (Exception e) {
				break;
			}
			
			adapter.addAdapter(new SimpleAdapter(
		    		context, 
		    		list, 
		    		R.layout.account_item, 
		    		new String[] {"currency","value"}, 
		    		new int[] {R.id.txt_currency, R.id.txt_value}));
		}
		if(pln!=0) {
			item = new HashMap<String,String>();
			item.put("currency", "PLN");
			item.put("value", df.format(pln));
			summary.add(item);
		}
		if(eur!=0) {
			item = new HashMap<String,String>();
			item.put("currency", "EUR");
			item.put("value", df.format(eur));
			summary.add(item);
		}
		if(usd!=0) {
			item = new HashMap<String,String>();
			item.put("currency", "USD");
			item.put("value", df.format(usd));
			summary.add(item);
		}
		if(gbp!=0) {
			item = new HashMap<String,String>();
			item.put("currency", "GBP");
			item.put("value", df.format(gbp));
			summary.add(item);
		}
		if(chf!=0) {
			item = new HashMap<String,String>();
			item.put("currency", "CHF");
			item.put("value", df.format(chf));
			summary.add(item);
		}
	}
}
