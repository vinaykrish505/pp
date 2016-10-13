package com.example.adira;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

	SharedPreferences sharedPreferences;
	public static String mailidusername = "nearbylookupf@gmail.com";
	public static String mailidpassword = "Alpha@version";
	public static String mailidto = "nearbylookupt@gmail.com";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		// if (!isMyServiceRunning(MyService.class, getApplicationContext())) {
		// startService(new Intent(SplashActivity.this, MyService.class));
		// }
		sharedPreferences = getSharedPreferences("signupactivity", Context.MODE_PRIVATE);
		new Handler().postDelayed(new Runnable() {
			boolean signup = sharedPreferences.getBoolean("signup", false);

			@Override
			public void run() {
				if (signup) {
					Intent i = new Intent(SplashActivity.this, MainActivity.class);
					startActivity(i);
					finish();
				} else {
					Intent i = new Intent(SplashActivity.this, SignupActivity.class);
					startActivity(i);
					finish();
				}
			}
		}, 3000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				System.out.println("Services -> " + service.service.getClassName());
				return true;
			}
		}
		return false;
	}
}
