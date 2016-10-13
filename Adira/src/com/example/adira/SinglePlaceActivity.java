package com.example.adira;

import java.util.Locale;

import com.example.adira.PlaceDistance.Elements;
import com.example.adira.PlaceDistance.Rows;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SinglePlaceActivity extends Activity {
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;
	Button showonmap;

	// Place Details
	PlaceDetails placeDetails;
	PlaceDistance placeDistance;

	// Progress dialog
	ProgressDialog pDialog;

	// KEY Strings
	public static String KEY_REFERENCE = "reference";
	public static String KEY_NAME = "name";// id of the place
	ActionBar actionBar;
	TextView headerText;
	String latitude;
	String langitude;
	String li_name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_place);
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9612b2")));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.customactionbar);
		
		headerText = ((TextView) findViewById(R.id.mytext));
		headerText.setSingleLine(true);
		Intent i = getIntent();

		// Place referece id
		String reference = i.getStringExtra(KEY_REFERENCE);
		li_name= i.getStringExtra(KEY_NAME);
		headerText.setText(li_name);
		// Calling a Async Background thread
		new LoadSinglePlaceDetails().execute(reference);
	}

	/**
	 * Background Async Task to Load Google places
	 */
	class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SinglePlaceActivity.this);
			pDialog.setMessage("Loading profile ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Profile JSON
		 */
		protected String doInBackground(String... args) {
			String reference = args[0];

			// creating Places class object
			googlePlaces = new GooglePlaces();

			// Check if used is connected to Internet
			try {
				placeDetails = googlePlaces.getPlaceDetails(reference);
				placeDistance = googlePlaces.getPlaceDistance(placeDetails);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 */
					if (placeDetails != null) {
						String status = placeDetails.status;

						// check place deatils status
						// Check for all possible status
						if (status.equals("OK")) {
							if (placeDetails.result != null) {
								String name = placeDetails.result.name;
								String address = placeDetails.result.formatted_address;
								String phone = placeDetails.result.formatted_phone_number;
								latitude = Double.toString(placeDetails.result.geometry.location.lat);
								langitude = Double.toString(placeDetails.result.geometry.location.lng);

								Log.d("Place ", name + address + phone + latitude + langitude);

								// Displaying all the details in the view
								// single_place.xml
								TextView lbl_name = (TextView) findViewById(R.id.name);
								TextView lbl_address = (TextView) findViewById(R.id.address);
								TextView lbl_phone = (TextView) findViewById(R.id.phone);
								TextView lbl_location = (TextView) findViewById(R.id.location);

								// Check for null data from google
								// Sometimes place details might missing
								name = name == null ? "Not present" : name; // if
																			// name
																			// is
																			// null
																			// display
																			// as
																			// "Not
																			// present"
								address = address == null ? "Not present" : address;
								phone = phone == null ? "Not present" : phone;
								latitude = latitude == null ? "Not present" : latitude;
								langitude = langitude == null ? "Not present" : langitude;

								lbl_name.setText(name);
								lbl_address.setText(address);
								lbl_phone.setText(Html.fromHtml("<b>Phone:</b> " + phone));
								lbl_location.setText(Html
										.fromHtml("<b>Latitude:</b> " + latitude + ", <b>Longitude:</b> " + langitude));
							}
						} else if (status.equals("ZERO_RESULTS")) {
							alert.showAlertDialog(SinglePlaceActivity.this, "Near Places", "Sorry no place found.",
									false);
						} else if (status.equals("UNKNOWN_ERROR")) {
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry unknown error occured.", false);
						} else if (status.equals("OVER_QUERY_LIMIT")) {
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry query limit to google places is reached", false);
						} else if (status.equals("REQUEST_DENIED")) {
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry error occured. Request is denied", false);
						} else if (status.equals("INVALID_REQUEST")) {
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry error occured. Invalid Request", false);
						} else {
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error", "Sorry error occured.",
									false);
						}
						if (placeDistance != null) {
							String status1 = placeDistance.status;
							if (status1.equalsIgnoreCase("OK")) {
								Rows rows = placeDistance.rows.get(0);
								Elements elements = rows.elements.get(0);
								String distance = elements.distance.text;
								String duration = elements.duration.text;

								TextView lbl_distance = (TextView) findViewById(R.id.distance);
								TextView lbl_duration = (TextView) findViewById(R.id.duration);

								lbl_distance.setText(Html.fromHtml(
										"<b>Distance:</b> " + distance));
								lbl_duration.setText(Html.fromHtml("<b>Duration:</b> " + duration));

							}
						}

					} else {
						alert.showAlertDialog(SinglePlaceActivity.this, "Places Error", "Sorry error occured.", false);
					}

				}
			});
			showonmap = (Button)findViewById(R.id.showOnMap);
			showonmap.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String uri_ = "http://maps.google.com/maps?q=loc:" + latitude + "," + langitude + " (" + li_name + ")";
//					String uri = String.format(Locale.ENGLISH, "geo:0.0,0.0", latitude, langitude);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri_));
					startActivity(intent);			
				}
			});
		}

	}

}
