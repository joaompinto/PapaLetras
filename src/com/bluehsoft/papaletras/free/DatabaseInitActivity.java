package com.bluehsoft.papaletras.free;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class DatabaseInitActivity extends Activity {
	private static final String TAG = "DatabaseInitActivity";
	static String mGameMode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);   

    	mGameMode = getIntent().getStringExtra("mode");
    	
        final String db_filename = LargeAsset.getDatabasePath(this, "wordlist");
        final LargeAsset large_asset  = new LargeAsset(this, db_filename, "wordlist_");
        
        boolean needs_db_copy = false;
        if(large_asset.file_exists()) {
        	String current_version = getDBVersion();        	
        	Log.d(TAG, "DB version: "+current_version);
        	Log.d(TAG, "Asset version: "+asset_db_version());
        	if(!current_version.equals(asset_db_version()))
        		needs_db_copy = true;
        } else {
        	needs_db_copy = true;
        }    	
        if(needs_db_copy) {
        	setContentView(R.layout.init_db);      
        	new initWordListDBTask().execute(large_asset);
        } else {
        	startGame();
        }
    }
    
    private void startGame() {
		Intent myIntent = new Intent(DatabaseInitActivity.this, PlayGameActivity.class);
		myIntent.putExtra("mode", mGameMode);
		startActivity(myIntent);	
		finish();
    }
    
    private String getDBVersion() {  
    	
    	DatabaseHelper mOpenHelper = new DatabaseHelper(this, "wordlist");
    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    	
		String p_query = "select data from version_info";
		Cursor cursor = db.rawQuery(p_query, null);
		cursor.moveToFirst();
		String db_version = cursor.getString(0);
		
		cursor.close();
		db.close();
		mOpenHelper.close();
		
		return db_version;
    }    
    
	String asset_db_version ()
	{
		InputStreamReader reader = null; 
		try {
			reader = new InputStreamReader(this.getAssets().open("wordlist.version"));
		} catch (IOException e) {
			// File not found, it's a new install 
			e.printStackTrace();
			
		}		
		BufferedReader br = new BufferedReader(reader, 64);
		String asset_version = null;
		try {
			asset_version = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return asset_version;
	}
		
	private class initWordListDBTask extends AsyncTask<LargeAsset, Void, Integer> {
		@Override
		protected Integer doInBackground(LargeAsset... large_asset) {
			for(LargeAsset asset: large_asset)
				try {
					asset.build();
				} catch (IOException e) {
					e.printStackTrace();
				}
			return 0;
		}

		protected void onPostExecute(Integer result) {
			startGame();
		}
	}
}
