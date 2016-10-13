package com.example.adira;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class Sqldata extends SQLiteOpenHelper {
	static String DB_PATH = null;
	private final Context myContext;
	public SQLiteDatabase myDataBase;
	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "adhira.sqlite";
	// Datebase Tables
	public static final String TABLE_MAILQUEUE = "mailqueue";

	// Database Mailqueue Table Columns
	public static final String KEY_MAILQUEUE_ROW_ID = "rowid";
	public static final String KEY_MAILQUEUE_SUBJECT = "subject";
	public static final String KEY_MAILQUEUE_BODY = "body";

	// Emailqueue Table Create Statement
	private static final String CREATE_TABLE_MAILQUEUE = "CREATE TABLE IF NOT EXISTS " + TABLE_MAILQUEUE + "("
			+ KEY_MAILQUEUE_ROW_ID + " INTEGER PRIMARY KEY," + KEY_MAILQUEUE_SUBJECT + " TEXT," + KEY_MAILQUEUE_BODY
			+ " TEXT," + ");";

	public Sqldata(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		System.out.println("Context ***** > "+context);
		this.myContext = context;
		DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 */
	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getReadableDatabase();

			try {
				copyDataBase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	public static boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		} catch (SQLiteException e) {

			// database does't exist yet.
			e.printStackTrace();
		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 */
	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DATABASE_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null) {
			myDataBase.close();
		}

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
