package com.bluehsoft.papaletras.free;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ScoreShowActivity extends Activity {

	private PapaLetrasApplication mApp;
	private ScoreDatabase mScoreDatabase;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.score_show);
    	
    	mApp = (PapaLetrasApplication) this.getApplication();
    	mScoreDatabase = mApp.getScoreDB(); 
    	
    	//String name = getIntent().getStringExtra("name");
    	//int score = getIntent().getIntExtra("score", 0);    	 
    	int pos = getIntent().getIntExtra("pos", -1);
    	 	
    	final int size = mScoreDatabase.getTopTen().size();
    	
    	TableLayout TopTenTable = (TableLayout) findViewById(R.id.TopTenTable);
    	for(int i = 0; i < size; ++i) {
    		TextView tv;
    		TopTenEntry item = mScoreDatabase.getTopTen().get(i);
    		TableRow row = new TableRow(this);

    		tv = new TextView(this);    		
    		tv.setText(new Integer(i+1).toString()+ "º");
    		tv.setPadding(5, 5, 5, 5);  		
    		tv.setGravity(Gravity.CENTER);
    		row.addView(tv);    		
    		
    		tv = new TextView(this);
    		tv.setTextSize(15);    		    		
    		tv.setText(item.name);
    		tv.setPadding(5, 5, 5, 5);
    		tv.setGravity(Gravity.LEFT);
    		row.addView(tv);    		

    		tv = new TextView(this);    		
    		tv.setText(new Integer(item.score).toString());
    		tv.setPadding(5, 5, 5, 5);
    		tv.setGravity(Gravity.CENTER);
    		row.addView(tv); 
    		if(i == pos-1)
    			row.setBackgroundColor(Color.BLUE);
    		
    		TopTenTable.addView(row);
    	}
    }
    
    public void ScoresBackButton_OnClick(View v) {
    	finish();
    }    
}
