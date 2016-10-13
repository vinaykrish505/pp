package com.example.adira;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import android.content.Context;
import android.util.Log;

@SuppressWarnings("deprecation")
public class GooglePlaces {

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	// Google API Key
	private static final String API_KEY = "AIzaSyAEjJLalCm-jmGit7W9fbXWXDuMDMA-yqk"; // place your API key here

	// Google Places serach url's
	private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	private static final String PLACES_DISTANCE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?";

	private double _latitude;
	private double _longitude;
	private double _radius;
	
	/**
	 * Searching places
	 * @param latitude - latitude of place
	 * @params longitude - longitude of place
	 * @param radius - radius of searchable area
	 * @param types - type of place to search
	 * @return list of places
	 * */
	public PlacesList search(double latitude, double longitude, double radius, String types)
			throws Exception {

		this._latitude = latitude;
		this._longitude = longitude;
		this._radius = radius;

		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("location", _latitude + "," + _longitude);
			request.getUrl().put("radius", _radius); // in meters
			request.getUrl().put("sensor", "false");
			if(types != null)
				request.getUrl().put("types", types);
System.out.println("url -> "+request.getUrl());
			PlacesList list = request.execute().parseAs(PlacesList.class);
			// Check log cat for places response status
			Log.d("Places Status", "" + list.status);
			return list;

		} catch (Exception e) {
			Log.e("Error:", e.getMessage());
			return null;
		}

	}

	/**
	 * Searching single place full details
	 * @param refrence - reference id of place
	 * 				   - which you will get in search api request
	 * */
	public PlaceDetails getPlaceDetails(String reference) throws Exception {
		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("reference", reference);
			request.getUrl().put("sensor", "false");
			System.out.println("url -> "+request.getUrl());
			PlaceDetails place = request.execute().parseAs(PlaceDetails.class);
			
			return place;

		} catch (Exception e) {
			Log.e("Error in Perform Details", e.getMessage());
			throw e;
		}
	}

	public PlaceDistance getPlaceDistance(PlaceDetails details) throws Exception {
		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_DISTANCE_URL));
			request.getUrl().put("key", API_KEY);
			String origin = MainActivity.latitude+","+MainActivity.longitude;
			String destination = details.result.geometry.location.lat+","+details.result.geometry.location.lng;
			request.getUrl().put("origins", origin);
			request.getUrl().put("destinations", destination);
			System.out.println("url -> "+request.getUrl());
			PlaceDistance place = request.execute().parseAs(PlaceDistance.class);
			return place;

		} catch (Exception e) {
			Log.e("Error in Perform Details", e.getMessage());
			throw e;
		}
	}

	
	/**
	 * Creating http request Factory
	 * */
	public static HttpRequestFactory createRequestFactory(
			final HttpTransport transport) {
		return transport.createRequestFactory(new HttpRequestInitializer() {
			public void initialize(HttpRequest request) {
				GoogleHeaders headers = new GoogleHeaders();
				headers.setApplicationName("AndroidHive-Places-Test");
				request.setHeaders(headers);
				JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
				request.addParser(parser);
			}
		});
	}

}
