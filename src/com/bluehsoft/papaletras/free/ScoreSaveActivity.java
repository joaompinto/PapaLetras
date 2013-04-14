package com.bluehsoft.papaletras.free;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

public class ScoreSaveActivity extends Activity implements OnKeyListener, TextWatcher {
	private static final String TAG = "ScoreSaveActivity";

	public static final String PREFS_NAME = "MyPrefsFile";	
	
	private int mScore;
	private int mPlayTime;
	
	private PapaLetrasApplication mApp;
	private ScoreDatabase mScoreDatabase;
 
	EditText mScoreName;
	Button mScoreSaveButton;
	
	TableRow mTopTenRow;
	TextView mTopTenPos;
	
	int m_top_ten_pos = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.score_save);
    	
    	mApp = (PapaLetrasApplication) this.getApplication();
    	mScoreDatabase = mApp.getScoreDB();

    	mTopTenRow = (TableRow) findViewById(R.id.TopTenRow);
    	mTopTenPos = (TextView) findViewById(R.id.TopTenPos);
    	mScoreName = (EditText) findViewById(R.id.score_name);
    	mScoreName.addTextChangedListener(this);
    	mScoreSaveButton = (Button) findViewById(R.id.ScoreSaveButton);
    	
    	mScoreName.setOnKeyListener(this);
    	mScore = getIntent().getIntExtra("score", 0);
    	mPlayTime = getIntent().getIntExtra("playtime", 0); 
    	//mAnswers = getIntent().getStringExtra("words");

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastScoreName = settings.getString("lastScoreName", "");
        mScoreName.setText(lastScoreName);
    	
    	if(mScore < 1)
    		finish();
    	else
    		checkAchievements();
    }
    
    private void checkAchievements () {
    	int top_ten_pos = mScoreDatabase.TopTenPos(mScore);    	
    	if(top_ten_pos < 10) {
    		String score_str;
    		score_str = (mScore == 1) ? "ponto" : "pontos";  			
    		mTopTenPos.setText(new Integer(top_ten_pos+1).toString()+"º"+" ("+mScore+" "+score_str+")");
    		mTopTenRow.setVisibility(View.VISIBLE);
    	} else
    		finish();
    }

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {		
		if ((event.getAction() == KeyEvent.ACTION_DOWN) && 
				(keyCode == KeyEvent.KEYCODE_ENTER) &&
				(mScoreName.length() > 2)) {
					ScoreSaveButton_OnClick(mScoreSaveButton);
		}
		return false;
	}
	
    
	public void ScoreSaveButton_OnClick(View v) {
		Log.d(TAG, "ScoreSaveButton_OnClick");
		String name = mScoreName.getText().toString();
		mScoreDatabase.save(name, mScore, mPlayTime);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();		
		editor.putString("lastScoreName", name);
		editor.commit();
	
		Intent myIntent = new Intent(ScoreSaveActivity.this, ScoreShowActivity.class);
		myIntent.putExtra("name", name);
		myIntent.putExtra("score", mScore);
		myIntent.putExtra("pos", mScoreDatabase.TopTenPos(mScore));
		startActivity(myIntent);		
		finish();
	}

    @Override
    public void afterTextChanged(Editable s) {
    	mScoreSaveButton.setEnabled(s.length()>2);
    }	
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
}
