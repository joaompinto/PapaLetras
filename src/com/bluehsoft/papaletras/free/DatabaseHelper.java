package com.bluehsoft.papaletras.free;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
*
* This class helps open, create, and upgrade the database file. Set to package visibility
* for testing purposes.
*/
class DatabaseHelper extends SQLiteOpenHelper {
	private static int mDatabaseVersion = 1;
	private static final String TAG = "DatabaseHelper";
	private String mCreateTablesSQL = null;
		
	DatabaseHelper(Context context, String DatabaseName) {
	   	
       // calls the super constructor, requesting the default cursor factory.
       super(context, DatabaseName, null, mDatabaseVersion);       
	}

	DatabaseHelper(Context context, String DatabaseName, String CreateTablesSQL, int mDatabaseVersion) {
	   	
	       // calls the super constructor, requesting the default cursor factory.
	       super(context, DatabaseName, null, mDatabaseVersion);  
	       mCreateTablesSQL = CreateTablesSQL;
		}	
	
   /**
    *
    * Creates the underlying database with table name and column names taken from the
    */
   @Override
   public void onCreate(SQLiteDatabase db) {
	   Log.d(TAG, "OnCreate");
	   if(mCreateTablesSQL != null)
		   db.execSQL(mCreateTablesSQL);
   }

   /**
    *
    * Demonstrates that the provider must consider what happens when the
    * underlying datastore is changed. In this sample, the database is upgraded the database
    * by destroying the existing data.
    * A real application should upgrade the database in place.
    */
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	   //
   }
}
