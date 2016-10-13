package com.example.adira;

import java.util.ArrayList;
import java.util.HashMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener, OnClickListener {

	private static final int REQUEST_CODE = 789;
	private DevicePolicyManager mDPM;
	private ComponentName mAdminName;
	GPSTracker gps;
	static Context context;
	int GPS_REQUESTCODE_LOW = 101;
	LocationManager locationManager;
	static double latitude;
	static double longitude;
	PlacesList nearPlaces;
	Button choose, search;
	ListView lv;
	ActionBar actionBar;
	TextView headerText;
	View promptsView;
	AlertDialog chooseOptionDialog;
	ProgressDialog pDialog;
	GooglePlaces googlePlaces;
	ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();
	public static String KEY_REFERENCE = "reference"; // id of the place
	public static String KEY_NAME = "name"; // name of the place
	public static String KEY_VICINITY = "vicinity"; // Place area name
	
	Button tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10, tv11, tv12, tv13, tv14, tv15, tv16, tv17, tv18, tv19,
			tv20, tv21, tv22, tv23, tv24, tv25, tv26, tv27, tv28, tv29, tv30, tv31, tv32, tv33, tv34, tv35, tv36, tv37,
			tv38, tv39, tv40, tv41, tv42, tv43, tv44, tv45, tv46, tv47, tv48, tv49, tv50, tv51, tv52, tv53, tv54, tv55,
			tv56, tv57, tv58, tv59, tv60, tv61, tv62, tv63, tv64, tv65, tv66, tv67, tv68, tv69, tv70, tv71, tv72, tv73,
			tv74, tv75, tv76, tv77, tv78, tv79, tv80, tv81, tv82, tv83, tv84, tv85, tv86, tv87, tv88, tv89, tv90, tv91;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		gps = new GPSTracker(context);
		if (gps.canGetLocation()) {
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9612b2")));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.customactionbar);
		setContentView(R.layout.activity_main);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9612b2")));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.customactionbar);
		headerText = ((TextView) findViewById(R.id.mytext));
		headerText.setSingleLine(true);
		context = this;
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		gps = new GPSTracker(MainActivity.this);
		Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
		if (gps.canGetLocation()) {
			Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			// moveCamera();
		} else {
			buildAlertMessageNoGps();
		}
		choose = (Button) findViewById(R.id.button1);
		search = (Button) findViewById(R.id.button2);
		lv = (ListView) findViewById(R.id.listview);

		choose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chooseOption();
			}
		});

		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (gps != null) {
					latitude = gps.getLatitude();
					longitude = gps.getLongitude();
					if (isConnectingToInternet()) {
						if (headerText.getText().toString().trim().length() != 0) {
							new LoadPlaces().execute();
						} else {
							Toast.makeText(context, "Choose Place and Go", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(context, "Couldn't connect to Internet", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context, "Couldn't get current location", Toast.LENGTH_SHORT).show();
				}
			}
		});
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// getting values from selected ListItem
				String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
				String namelist = ((TextView) view.findViewById(R.id.name)).getText().toString();
				// Starting new intent
				Intent in = new Intent(context, SinglePlaceActivity.class);

				// Sending place refrence id to single place activity
				// place refrence id used to get "Place full details"
				in.putExtra(KEY_NAME, namelist);
				in.putExtra(KEY_REFERENCE, reference);
				startActivity(in);
			}
		});
		try {
			// Initiate DevicePolicyManager.
			mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			mAdminName = new ComponentName(this, DeviceAdminDemo.class);

			if (!mDPM.isAdminActive(mAdminName)) {
				Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
				intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
						"Click on Activate button to secure your application.");
				startActivityForResult(intent, REQUEST_CODE);
			} else {
				Intent intent = new Intent(MainActivity.this, MyService.class);
				startService(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void buildAlertMessageNoGps() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?");
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUESTCODE_LOW);
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		System.out.println(
				"requestcode (arg0) -> " + arg0 + "\tresultcode (arg1) -> " + arg1 + "\tdata (arg2) -> " + arg2);
		if (arg0 == REQUEST_CODE) {
			if (arg1 == RESULT_OK) {
				Intent intent = new Intent(MainActivity.this, MyService.class);
				startService(intent);
			} else {
				Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
				intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
						"Click on Activate button to secure your application.");
				startActivityForResult(intent, REQUEST_CODE);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
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

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	
	public void initializeTextviews() {
		LayoutInflater li = LayoutInflater.from(this);
		promptsView = li.inflate(R.layout.dialog_places, null);
		tv1 = (Button) promptsView.findViewById(R.id.textView1);
		tv1.setOnClickListener(this);
		tv2 = (Button) promptsView.findViewById(R.id.textView2);
		tv2.setOnClickListener(this);
		tv3 = (Button) promptsView.findViewById(R.id.textView3);
		tv3.setOnClickListener(this);
		tv4 = (Button) promptsView.findViewById(R.id.textView4);
		tv4.setOnClickListener(this);
		tv5 = (Button) promptsView.findViewById(R.id.textView5);
		tv5.setOnClickListener(this);
		tv6 = (Button) promptsView.findViewById(R.id.textView6);
		tv6.setOnClickListener(this);
		tv7 = (Button) promptsView.findViewById(R.id.textView7);
		tv7.setOnClickListener(this);
		tv8 = (Button) promptsView.findViewById(R.id.textView8);
		tv8.setOnClickListener(this);
		tv9 = (Button) promptsView.findViewById(R.id.textView9);
		tv9.setOnClickListener(this);
		tv10 = (Button) promptsView.findViewById(R.id.textView10);
		tv10.setOnClickListener(this);
		tv11 = (Button) promptsView.findViewById(R.id.textView11);
		tv11.setOnClickListener(this);
		tv12 = (Button) promptsView.findViewById(R.id.textView12);
		tv12.setOnClickListener(this);
		tv13 = (Button) promptsView.findViewById(R.id.textView13);
		tv13.setOnClickListener(this);
		tv14 = (Button) promptsView.findViewById(R.id.textView14);
		tv14.setOnClickListener(this);
		tv15 = (Button) promptsView.findViewById(R.id.textView15);
		tv15.setOnClickListener(this);
		tv16 = (Button) promptsView.findViewById(R.id.textView16);
		tv16.setOnClickListener(this);
		tv17 = (Button) promptsView.findViewById(R.id.textView17);
		tv17.setOnClickListener(this);
		tv18 = (Button) promptsView.findViewById(R.id.textView18);
		tv18.setOnClickListener(this);
		tv19 = (Button) promptsView.findViewById(R.id.textView19);
		tv19.setOnClickListener(this);
		tv20 = (Button) promptsView.findViewById(R.id.textView20);
		tv20.setOnClickListener(this);
		tv21 = (Button) promptsView.findViewById(R.id.textView21);
		tv21.setOnClickListener(this);
		tv22 = (Button) promptsView.findViewById(R.id.textView22);
		tv22.setOnClickListener(this);
		tv23 = (Button) promptsView.findViewById(R.id.textView23);
		tv23.setOnClickListener(this);
		tv24 = (Button) promptsView.findViewById(R.id.textView24);
		tv24.setOnClickListener(this);
		tv25 = (Button) promptsView.findViewById(R.id.textView25);
		tv25.setOnClickListener(this);
		tv26 = (Button) promptsView.findViewById(R.id.textView26);
		tv26.setOnClickListener(this);
		tv27 = (Button) promptsView.findViewById(R.id.textView27);
		tv27.setOnClickListener(this);
		tv28 = (Button) promptsView.findViewById(R.id.textView28);
		tv28.setOnClickListener(this);
		tv29 = (Button) promptsView.findViewById(R.id.textView29);
		tv29.setOnClickListener(this);
		tv30 = (Button) promptsView.findViewById(R.id.textView30);
		tv30.setOnClickListener(this);
		tv31 = (Button) promptsView.findViewById(R.id.textView31);
		tv31.setOnClickListener(this);
		tv32 = (Button) promptsView.findViewById(R.id.textView32);
		tv32.setOnClickListener(this);
		tv33 = (Button) promptsView.findViewById(R.id.textView33);
		tv33.setOnClickListener(this);
		tv34 = (Button) promptsView.findViewById(R.id.textView34);
		tv34.setOnClickListener(this);
		tv35 = (Button) promptsView.findViewById(R.id.textView35);
		tv35.setOnClickListener(this);
		tv36 = (Button) promptsView.findViewById(R.id.textView36);
		tv36.setOnClickListener(this);
		tv37 = (Button) promptsView.findViewById(R.id.textView37);
		tv37.setOnClickListener(this);
		tv38 = (Button) promptsView.findViewById(R.id.textView38);
		tv38.setOnClickListener(this);
		tv39 = (Button) promptsView.findViewById(R.id.textView39);
		tv39.setOnClickListener(this);
		tv40 = (Button) promptsView.findViewById(R.id.textView40);
		tv40.setOnClickListener(this);
		tv41 = (Button) promptsView.findViewById(R.id.textView41);
		tv41.setOnClickListener(this);
		tv42 = (Button) promptsView.findViewById(R.id.textView42);
		tv42.setOnClickListener(this);
		tv43 = (Button) promptsView.findViewById(R.id.textView43);
		tv43.setOnClickListener(this);
		tv44 = (Button) promptsView.findViewById(R.id.textView44);
		tv44.setOnClickListener(this);
		tv45 = (Button) promptsView.findViewById(R.id.textView45);
		tv45.setOnClickListener(this);
		tv46 = (Button) promptsView.findViewById(R.id.textView46);
		tv46.setOnClickListener(this);
		tv47 = (Button) promptsView.findViewById(R.id.textView47);
		tv47.setOnClickListener(this);
		tv48 = (Button) promptsView.findViewById(R.id.textView48);
		tv48.setOnClickListener(this);
		tv49 = (Button) promptsView.findViewById(R.id.textView49);
		tv49.setOnClickListener(this);
		tv50 = (Button) promptsView.findViewById(R.id.textView50);
		tv50.setOnClickListener(this);
		tv51 = (Button) promptsView.findViewById(R.id.textView51);
		tv51.setOnClickListener(this);
		tv52 = (Button) promptsView.findViewById(R.id.textView52);
		tv52.setOnClickListener(this);
		tv53 = (Button) promptsView.findViewById(R.id.textView53);
		tv53.setOnClickListener(this);
		tv54 = (Button) promptsView.findViewById(R.id.textView54);
		tv54.setOnClickListener(this);
		tv55 = (Button) promptsView.findViewById(R.id.textView55);
		tv55.setOnClickListener(this);
		tv56 = (Button) promptsView.findViewById(R.id.textView56);
		tv56.setOnClickListener(this);
		tv57 = (Button) promptsView.findViewById(R.id.textView57);
		tv57.setOnClickListener(this);
		tv58 = (Button) promptsView.findViewById(R.id.textView58);
		tv58.setOnClickListener(this);
		tv59 = (Button) promptsView.findViewById(R.id.textView59);
		tv59.setOnClickListener(this);
		tv60 = (Button) promptsView.findViewById(R.id.textView60);
		tv60.setOnClickListener(this);
		tv61 = (Button) promptsView.findViewById(R.id.textView61);
		tv61.setOnClickListener(this);
		tv62 = (Button) promptsView.findViewById(R.id.textView62);
		tv62.setOnClickListener(this);
		tv63 = (Button) promptsView.findViewById(R.id.textView63);
		tv63.setOnClickListener(this);
		tv64 = (Button) promptsView.findViewById(R.id.textView64);
		tv64.setOnClickListener(this);
		tv65 = (Button) promptsView.findViewById(R.id.textView65);
		tv65.setOnClickListener(this);
		tv66 = (Button) promptsView.findViewById(R.id.textView66);
		tv66.setOnClickListener(this);
		tv67 = (Button) promptsView.findViewById(R.id.textView67);
		tv67.setOnClickListener(this);
		tv68 = (Button) promptsView.findViewById(R.id.textView68);
		tv68.setOnClickListener(this);
		tv69 = (Button) promptsView.findViewById(R.id.textView69);
		tv69.setOnClickListener(this);
		tv70 = (Button) promptsView.findViewById(R.id.textView70);
		tv70.setOnClickListener(this);
		tv71 = (Button) promptsView.findViewById(R.id.textView71);
		tv71.setOnClickListener(this);
		tv72 = (Button) promptsView.findViewById(R.id.textView72);
		tv72.setOnClickListener(this);
		tv73 = (Button) promptsView.findViewById(R.id.textView73);
		tv73.setOnClickListener(this);
		tv74 = (Button) promptsView.findViewById(R.id.textView74);
		tv74.setOnClickListener(this);
		tv75 = (Button) promptsView.findViewById(R.id.textView75);
		tv75.setOnClickListener(this);
		tv76 = (Button) promptsView.findViewById(R.id.textView76);
		tv76.setOnClickListener(this);
		tv77 = (Button) promptsView.findViewById(R.id.textView77);
		tv77.setOnClickListener(this);
		tv78 = (Button) promptsView.findViewById(R.id.textView78);
		tv78.setOnClickListener(this);
		tv79 = (Button) promptsView.findViewById(R.id.textView79);
		tv79.setOnClickListener(this);
		tv80 = (Button) promptsView.findViewById(R.id.textView80);
		tv80.setOnClickListener(this);
		tv81 = (Button) promptsView.findViewById(R.id.textView81);
		tv81.setOnClickListener(this);
		tv82 = (Button) promptsView.findViewById(R.id.textView82);
		tv82.setOnClickListener(this);
		tv83 = (Button) promptsView.findViewById(R.id.textView83);
		tv83.setOnClickListener(this);
		tv84 = (Button) promptsView.findViewById(R.id.textView84);
		tv84.setOnClickListener(this);
		tv85 = (Button) promptsView.findViewById(R.id.textView85);
		tv85.setOnClickListener(this);
		tv86 = (Button) promptsView.findViewById(R.id.textView86);
		tv86.setOnClickListener(this);
		tv87 = (Button) promptsView.findViewById(R.id.textView87);
		tv87.setOnClickListener(this);
		tv88 = (Button) promptsView.findViewById(R.id.textView88);
		tv88.setOnClickListener(this);
		tv89 = (Button) promptsView.findViewById(R.id.textView89);
		tv89.setOnClickListener(this);
		tv90 = (Button) promptsView.findViewById(R.id.textView90);
		tv90.setOnClickListener(this);
		tv91 = (Button) promptsView.findViewById(R.id.textView91);
		tv91.setOnClickListener(this);
	}

	public void chooseOption() {
		initializeTextviews();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptsView);
		alertDialogBuilder.setTitle("Choose Place");
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		chooseOptionDialog = alertDialogBuilder.create();
		chooseOptionDialog.setCancelable(false);
		chooseOptionDialog.show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.textView1:
			headerText.setText(tv1.getText());
			break;
		case R.id.textView2:
			headerText.setText(tv2.getText());
			break;
		case R.id.textView3:
			headerText.setText(tv3.getText());
			break;
		case R.id.textView4:
			headerText.setText(tv4.getText());
			break;
		case R.id.textView5:
			headerText.setText(tv5.getText());
			break;
		case R.id.textView6:
			headerText.setText(tv6.getText());
			break;
		case R.id.textView7:
			headerText.setText(tv7.getText());
			break;
		case R.id.textView8:
			headerText.setText(tv8.getText());
			break;
		case R.id.textView9:
			headerText.setText(tv9.getText());
			break;
		case R.id.textView10:
			headerText.setText(tv10.getText());
			break;
		case R.id.textView11:
			headerText.setText(tv11.getText());
			break;
		case R.id.textView12:
			headerText.setText(tv12.getText());
			break;
		case R.id.textView13:
			headerText.setText(tv13.getText());
			break;
		case R.id.textView14:
			headerText.setText(tv14.getText());
			break;
		case R.id.textView15:
			headerText.setText(tv15.getText());
			break;
		case R.id.textView16:
			headerText.setText(tv16.getText());
			break;
		case R.id.textView17:
			headerText.setText(tv17.getText());
			break;
		case R.id.textView18:
			headerText.setText(tv18.getText());
			break;
		case R.id.textView19:
			headerText.setText(tv19.getText());
			break;
		case R.id.textView20:
			headerText.setText(tv20.getText());
			break;
		case R.id.textView21:
			headerText.setText(tv21.getText());
			break;
		case R.id.textView22:
			headerText.setText(tv22.getText());
			break;
		case R.id.textView23:
			headerText.setText(tv23.getText());
			break;
		case R.id.textView24:
			headerText.setText(tv24.getText());
			break;
		case R.id.textView25:
			headerText.setText(tv25.getText());
			break;
		case R.id.textView26:
			headerText.setText(tv26.getText());
			break;
		case R.id.textView27:
			headerText.setText(tv27.getText());
			break;
		case R.id.textView28:
			headerText.setText(tv28.getText());
			break;
		case R.id.textView29:
			headerText.setText(tv29.getText());
			break;
		case R.id.textView30:
			headerText.setText(tv30.getText());
			break;
		case R.id.textView31:
			headerText.setText(tv31.getText());
			break;
		case R.id.textView32:
			headerText.setText(tv32.getText());
			break;
		case R.id.textView33:
			headerText.setText(tv33.getText());
			break;
		case R.id.textView34:
			headerText.setText(tv34.getText());
			break;
		case R.id.textView35:
			headerText.setText(tv35.getText());
			break;
		case R.id.textView36:
			headerText.setText(tv36.getText());
			break;
		case R.id.textView37:
			headerText.setText(tv37.getText());
			break;
		case R.id.textView38:
			headerText.setText(tv38.getText());
			break;
		case R.id.textView39:
			headerText.setText(tv39.getText());
			break;
		case R.id.textView40:
			headerText.setText(tv40.getText());
			break;
		case R.id.textView41:
			headerText.setText(tv41.getText());
			break;
		case R.id.textView42:
			headerText.setText(tv42.getText());
			break;
		case R.id.textView43:
			headerText.setText(tv43.getText());
			break;
		case R.id.textView44:
			headerText.setText(tv44.getText());
			break;
		case R.id.textView45:
			headerText.setText(tv45.getText());
			break;
		case R.id.textView46:
			headerText.setText(tv46.getText());
			break;
		case R.id.textView47:
			headerText.setText(tv47.getText());
			break;
		case R.id.textView48:
			headerText.setText(tv48.getText());
			break;
		case R.id.textView49:
			headerText.setText(tv49.getText());
			break;
		case R.id.textView50:
			headerText.setText(tv50.getText());
			break;
		case R.id.textView51:
			headerText.setText(tv51.getText());
			break;
		case R.id.textView52:
			headerText.setText(tv52.getText());
			break;
		case R.id.textView53:
			headerText.setText(tv53.getText());
			break;
		case R.id.textView54:
			headerText.setText(tv54.getText());
			break;
		case R.id.textView55:
			headerText.setText(tv55.getText());
			break;
		case R.id.textView56:
			headerText.setText(tv56.getText());
			break;
		case R.id.textView57:
			headerText.setText(tv57.getText());
			break;
		case R.id.textView58:
			headerText.setText(tv58.getText());
			break;
		case R.id.textView59:
			headerText.setText(tv59.getText());
			break;
		case R.id.textView60:
			headerText.setText(tv60.getText());
			break;
		case R.id.textView61:
			headerText.setText(tv61.getText());
			break;
		case R.id.textView62:
			headerText.setText(tv62.getText());
			break;
		case R.id.textView63:
			headerText.setText(tv63.getText());
			break;
		case R.id.textView64:
			headerText.setText(tv64.getText());
			break;
		case R.id.textView65:
			headerText.setText(tv65.getText());
			break;
		case R.id.textView66:
			headerText.setText(tv66.getText());
			break;
		case R.id.textView67:
			headerText.setText(tv67.getText());
			break;
		case R.id.textView68:
			headerText.setText(tv68.getText());
			break;
		case R.id.textView69:
			headerText.setText(tv69.getText());
			break;
		case R.id.textView70:
			headerText.setText(tv70.getText());
			break;
		case R.id.textView71:
			headerText.setText(tv71.getText());
			break;
		case R.id.textView72:
			headerText.setText(tv72.getText());
			break;
		case R.id.textView73:
			headerText.setText(tv73.getText());
			break;
		case R.id.textView74:
			headerText.setText(tv74.getText());
			break;
		case R.id.textView75:
			headerText.setText(tv75.getText());
			break;
		case R.id.textView76:
			headerText.setText(tv76.getText());
			break;
		case R.id.textView77:
			headerText.setText(tv77.getText());
			break;
		case R.id.textView78:
			headerText.setText(tv78.getText());
			break;
		case R.id.textView79:
			headerText.setText(tv79.getText());
			break;
		case R.id.textView80:
			headerText.setText(tv80.getText());
			break;
		case R.id.textView81:
			headerText.setText(tv81.getText());
			break;
		case R.id.textView82:
			headerText.setText(tv82.getText());
			break;
		case R.id.textView83:
			headerText.setText(tv83.getText());
			break;
		case R.id.textView84:
			headerText.setText(tv84.getText());
			break;
		case R.id.textView85:
			headerText.setText(tv85.getText());
			break;
		case R.id.textView86:
			headerText.setText(tv86.getText());
			break;
		case R.id.textView87:
			headerText.setText(tv87.getText());
			break;
		case R.id.textView88:
			headerText.setText(tv88.getText());
			break;
		case R.id.textView89:
			headerText.setText(tv89.getText());
			break;
		case R.id.textView90:
			headerText.setText(tv90.getText());
			break;
		case R.id.textView91:
			headerText.setText(tv91.getText());
			break;
		}
		if (chooseOptionDialog.isShowing()) {
			chooseOptionDialog.cancel();
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@SuppressLint("NewApi")
	public static boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

	class LoadPlaces extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 */
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();

			try {
				// Separeate your place types by PIPE symbol "|"
				// If you want all types places make it as null
				// Check list of types supported by google
				//
				String types = headerText.getText().toString();

				// Radius in meters - increase this value if you don't find any
				// places
				double radius = 2000; // 1000 meters

				// get nearest places
				nearPlaces = googlePlaces.search(latitude, longitude, radius, types);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			if (!placesListItems.isEmpty()) {
				placesListItems.clear();
			}
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 */
					// Get json response status
					String status = nearPlaces.status;

					// Check for all possible status
					if (status.equals("OK")) {
						// Successfully got places details
						if (nearPlaces.results != null) {
							// loop through each place
							for (Place p : nearPlaces.results) {
								HashMap<String, String> map = new HashMap<String, String>();

								// Place reference won't display in listview -
								// it will be hidden
								// Place reference is used to get "place full
								// details"
								map.put(KEY_REFERENCE, p.reference);

								// Place name
								map.put(KEY_NAME, p.name);

								// adding HashMap to ArrayList
								placesListItems.add(map);
							}
							// list adapter
							ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
									R.layout.list_item, new String[] { KEY_REFERENCE, KEY_NAME },
									new int[] { R.id.reference, R.id.name });

							// Adding data into listview
							lv.setAdapter(adapter);
						}
					} else {
						if (!placesListItems.isEmpty()) {
							placesListItems.clear();
						}
						lv.invalidateViews();
						Toast.makeText(context, "Sorry no places found. Try to change the types of places",
								Toast.LENGTH_LONG).show();
					}
				}
			});

		}

	}
}
