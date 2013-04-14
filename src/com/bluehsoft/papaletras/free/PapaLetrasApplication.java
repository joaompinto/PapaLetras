package com.bluehsoft.papaletras.free;

import android.app.Application;

public class PapaLetrasApplication extends Application {	
	private static ScoreDatabase mScoreDB;
	
	@Override
	public void onCreate() {
		super.onCreate();		
		mScoreDB = new ScoreDatabase(this);
		mScoreDB.readData();
	}
	
	public ScoreDatabase getScoreDB() {
		return mScoreDB;
	}
}
