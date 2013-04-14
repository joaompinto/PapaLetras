package com.bluehsoft.papaletras.free;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

final class TopTen {	
	final static String tableName  = "topten";
	final static String tableDef = " (playtime INTEGER, name TEXT, score INTEGER);";
}

class TopTenEntry {
	public String name;
	public int score;
	TopTenEntry(String name, int score) {
		this.name = name;
		this.score = score;
	}	
}

final public class ScoreDatabase {
	private SQLiteDatabase m_db;
	private DatabaseHelper mOpenHelper;
	private final static int mDatabaseVersion = 1;
	private static ArrayList<TopTenEntry> mTopTen = new ArrayList<TopTenEntry>();
	
	private static int m_top_ten_pos = 0;

	ScoreDatabase(Context context) {
		//Context context = activity.getApplicationContext();
		mOpenHelper = new DatabaseHelper(context, "score",
				"CREATE TABLE "+TopTen.tableName+TopTen.tableDef,
				mDatabaseVersion
				);	
		m_db = mOpenHelper.getWritableDatabase();
			
	}
	
	void readData() {
		mTopTen.clear();
		String sql = "SELECT name, score FROM topten ORDER BY score DESC";
		Cursor cursor = m_db.rawQuery(sql, null);
		while(cursor.moveToNext()) {
			TopTenEntry entry = new TopTenEntry(cursor.getString(0),  cursor.getInt(1));
			mTopTen.add(entry);
		}
	}
	
	ArrayList<TopTenEntry> getTopTen() {
		return mTopTen;
	}
	
	int TopTenPos(int score) {
		int place_pos = mTopTen.size(); // If not better is the last
		for(int i=0; i < mTopTen.size(); ++i) {
			if (score > mTopTen.get(i).score) {
				place_pos = i;
				break;
			}				
		}	
		m_top_ten_pos = place_pos;
		return place_pos;
	}

	
	public void save(String name, int score, int playtime) {		
		if(m_top_ten_pos < 10) {					
			m_db.beginTransaction();
			try {
				if(mTopTen.size() > 9) {
					m_db.delete("topten", "score <= ?", 
							new String[] {new Integer(mTopTen.get(9).score).toString()});
					mTopTen.remove(9);
				}
				ContentValues initialValues = new ContentValues();
				initialValues.put("playtime", playtime);
				initialValues.put("name", name);
				initialValues.put("score", score);
				m_db.insert("topten", null, initialValues);
				mTopTen.add(m_top_ten_pos, new TopTenEntry(name, score));
				m_db.setTransactionSuccessful();
			} finally {
				m_db.endTransaction();
			}
		}		
	}
	
}
