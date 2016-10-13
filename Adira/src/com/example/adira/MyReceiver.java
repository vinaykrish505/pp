package com.example.adira;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("**********com.example.adira.MyReceiver*********");
		Intent myIntent = new Intent(context, MyService.class);
		context.startService(myIntent);
	}

}
