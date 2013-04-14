package com.bluehsoft.papaletras.free;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PlayGameActivity extends Activity {

	private static final String TAG = "PlayGameActivity";
	
	final private static BoardGameLogic  mBoardGameLogic = new BoardGameLogic();;
	
	public static final int STATE_RUNNING = 1;	
	public static final int STATE_GAMEOVER = 2;	

	/* GUI objects */	
	private static PlayBoard m_playboard;
	private static TextView m_word;
	private static TextView mPlayTime;
	private static TextView mScoreText;
	private static TextView m_text_status;	
	private static TextView m_start_button;
			
	final private static Handler m_Handler = new Handler();
	final private static Random m_generator = new Random();
	final private static ArrayList<String> m_answers = new ArrayList<String>();
	
	private static boolean m_must_reset_word = false;
	private static SQLiteDatabase m_db;
	private static int m_max_word_id = 0;
	private static int m_initial_allowedTime = 60;
	private static int m_allowedTime = m_initial_allowedTime;
	private static int m_allowedTime_millis = m_allowedTime*1000;
	private static long mPlayedTime = -1;	
	
	private static String mGameMode;
	private static long mStartTime = 0;	
	private static int mPlayState = 0;
	private static int mDemoState = 0;
	private static int m_demo_word_i = 0;
	private static int m_score = 0;
	private static boolean mPlayTime_visibility = true;
	private static boolean mTimeIsBlinking = false;
			
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	/* DEV
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
        */
        super.onCreate(savedInstanceState);   
        
        setContentView(R.layout.playgame);
                        
        mGameMode = getIntent().getStringExtra("mode");        
        
    	final DatabaseHelper mOpenHelper = new DatabaseHelper(this, "wordlist");
    	m_db = mOpenHelper.getReadableDatabase();
		 
	    m_playboard = (PlayBoard) findViewById(R.id.playboard);
	    m_playboard.setBoardSize(5 ,5);	  
	    mBoardGameLogic.setBoard(m_playboard);			    	
	     
	    m_word = (TextView) findViewById(R.id.word);
	    mPlayTime = (TextView) findViewById(R.id.play_time);
	    mScoreText = (TextView) findViewById(R.id.score);
	            	    
	    // Temporary components
	    m_text_status = (TextView) findViewById(R.id.status);
	    m_start_button = (Button) findViewById(R.id.restart_game);	 
	    
	    startGame(0);        	

    }
    
    private void startGame(int start_score) {
    	setState(STATE_RUNNING);    	
	    // Set allowed time
	    m_allowedTime = m_initial_allowedTime;
	    m_allowedTime_millis = m_allowedTime*1000;    	
    	m_score = start_score;
        m_answers.clear();            	
    	m_Handler.removeCallbacks(mUpdateTimeTask);
    	m_Handler.removeCallbacks(mBlinkTimeTask);
        if(mGameMode.equals("demo")) {
        	mDemoState = 0;
        	m_demo_word_i = 0;
        	mBoardGameLogic.startGame("SABONETE");
        	m_playboard.setClickable(false);
        	m_Handler.postDelayed(mDemoPlayTask, 1000);
        } else {
        	mBoardGameLogic.startGame(getRandomWord());
        	m_Handler.postDelayed(mUpdateTimeTask, 1000);
        	m_playboard.setClickable(true);
        	m_word.setClickable(true);
        }
        
        mScoreText.setText(new Integer(m_score).toString());
              
        m_text_status.setVisibility(View.INVISIBLE);
        m_start_button.setVisibility(View.INVISIBLE);
        m_word.setText("");         
        m_word.setEnabled(false);
        mPlayTime.setVisibility(View.VISIBLE);        
        
        m_playboard.setKeepScreenOn(true);       

        mStartTime = System.currentTimeMillis();                
    }
    
    public void board_OnClick(View v) {
        	// your code here
        	String current_word = mBoardGameLogic.getWord();
        	m_word.setText(current_word);
        	int action_img = 0; 
        	boolean can_swap = mBoardGameLogic.countSelected() == 2;
        	boolean can_check = current_word.length() > 3;
        	if(can_swap)
        		action_img = R.drawable.swap;
        	else if(can_check)
        		action_img = R.drawable.question;
        	m_word.setCompoundDrawablesWithIntrinsicBounds(0, 0, action_img, 0);
        	m_word.setEnabled(can_swap || can_check);
    }

    public void word_OnClick(View v) {
 		   if(mPlayState == STATE_RUNNING && m_text_status.getVisibility() == View.VISIBLE)
	 			  m_text_status.setVisibility(View.INVISIBLE);        	
        	String current_word = mBoardGameLogic.getWord();
        	String lower_current_word = current_word.toLowerCase();
        	if(mBoardGameLogic.countSelected() == 2) {        		
        		mBoardGameLogic.swap2();
        		m_word.setText("");
        		m_word.setEnabled(false);
        		m_word.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        		return;
        	}
        	/* if(m_answers.contains(lower_current_word)) {
        		showResult(current_word, "repetida", 0xAAFFFF00);
        	} else */{ 
	        	boolean is_correct = findWord(lower_current_word); 
	        	int play_score = lower_current_word.length();
	        	int play_time = 20;
        		if(lower_current_word.length() > 5) {
        			play_score += 10;
        			play_time += 30;
        		}	        		        		
	        	if(is_correct) {
	        		m_score += play_score;
	        		m_allowedTime += play_time;	        		
	        		m_allowedTime_millis = m_allowedTime * 1000;
	        		m_answers.add(lower_current_word);
	        		m_must_reset_word = true;
	            	showResult(current_word, "certa", 0xAA00FF00);	            	
	            	//if(m_answers.size() == 1)
	            	//	mPlayTime.setTextColor(0xFF00FF00);
	        	} else {	        		
	        		m_score -= play_score;
	        		/*
	        		long millis = System.currentTimeMillis() - mStartTime;
	        		m_allowedTime -= play_time;
	        		m_allowedTime_millis = m_allowedTime * 1000;
	        		long remaining = m_allowedTime-millis;
	        		if(!mTimeIsBlinking && remaining < 10)
	        			m_Handler.postDelayed(mUpdateTimeTask, 0); */
	        		showResult(current_word, "errada", 0xAAFF0000);
	        	}
        	}
        	mScoreText.setText(new Integer(m_score).toString());        	              	        	
			m_word.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_button_blank, 0);		
			m_word.setEnabled(false);			
    }

    public void restart_game_OnClick(View v) {
    	startGame(0);
    }    

    private boolean findWord(String word) {
    	String table = "words";
    	String columns[] = {"word"};
    	String selection = "word = ?";
    	String selectionArgs[] = {word};
    	
    	Cursor cursor = m_db.query(table, columns, selection, selectionArgs, null, null, null);
    	boolean was_found = cursor.getCount() > 0;
    	cursor.close();
    	return was_found;
    }    
    
    static private String getRandomWord() {    	    	
    	if(m_max_word_id == 0) {
    		String p_query = "select max(_id) from words";
    		Cursor cursor = m_db.rawQuery(p_query, null);
    		cursor.moveToFirst();
    		m_max_word_id = cursor.getInt(0);
    		cursor.close();
    	}    	
    	final Integer i = m_generator.nextInt(m_max_word_id);    	
    	final String p_query = "select word from words where _id = ?";
    	Cursor cursor = m_db.rawQuery(p_query, new String[] { i.toString() });
    	if(!cursor.moveToFirst())
    		Log.e(TAG, "Failed to get a random password");
    	final String word = cursor.getString(0);
    	cursor.close();
    	return word;
    }    
    
    private void showMessage(String message, int TextSize) {
    	m_text_status.setTextSize(TextSize);
		m_text_status.setBackgroundColor(0xAA0000FF);
		m_text_status.setText(message);
		m_text_status.setVisibility(View.VISIBLE); 
    }
    
    private void setState(int state) {
    	mPlayState = state;
    	switch(mPlayState) {
    		case STATE_GAMEOVER:
    			m_Handler.removeCallbacks(mDismissResultTask);
    			m_Handler.removeCallbacks(mUpdateTimeTask);  
    			m_Handler.removeCallbacks(mBlinkTimeTask);
    			mTimeIsBlinking = false;
    			showMessage("Fim de jogo", 40);
    			m_word.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_button_blank, 0);
    			m_word.setEnabled(false);
    			m_start_button.setVisibility(View.VISIBLE);
    			m_playboard.setKeepScreenOn(false);
    			m_playboard.setClickable(false);    			
    			mBoardGameLogic.clearAll();
    			//if(m_answers.size() > 0)
    			//	break;
   				ArrayList<PlayBoard.Coordinate> word = mBoardGameLogic.getMasterWord();
			   	String current_word = "";
			   	int size = mBoardGameLogic.getMasterWord().size();
				for(int i = 0; i < size; ++i) {
				   	PlayBoard.Coordinate pos ;
					pos = word.get(i);
					m_playboard.setTile(PlayBoard.SELECTED, 
						 pos.x, pos.y);
					current_word += m_playboard.getTextAtPos(pos.x, pos.y);
    			}
				m_word.setText(current_word);
				mPlayTime.setVisibility(View.VISIBLE);
				m_playboard.invalidate();	
				Intent myIntent = new Intent(PlayGameActivity.this, ScoreSaveActivity.class);
				myIntent.putExtra("score", m_score);
				myIntent.putExtra("playtime", m_allowedTime);
				String answer_list = "";
				for (String answer: m_answers) {	
					answer_list += ","+answer;
				}
				myIntent.putExtra("words", answer_list);
				startActivity(myIntent);
    			break;
    	}
    }
    
    private Runnable mUpdateTimeTask = new Runnable() {
	   public void run() {		   
	       long millis = System.currentTimeMillis() - mStartTime;
	       if(millis < 0)
	    	   millis = 0;
	       //Log.d(TAG, "millis = "+ millis);
	       int seconds = m_allowedTime - (int) (millis / 1000);
	       //Log.i("DEMO", "Elapsed seconds:" + seconds);
	       int minutes = seconds / 60;
	       seconds= seconds % 60;
	       
	       if (seconds < 10) {
	    	   mPlayTime.setText("" + minutes + ":0" + seconds);
	       } else {
	    	   mPlayTime.setText("" + minutes + ":" + seconds);            
	       }
	       if(m_allowedTime_millis - millis < 0) { // Game over
	    	   setState(STATE_GAMEOVER);
	       	}
	       else
	    	   m_Handler.postDelayed(this, 1000);
	       
	       
	      if (minutes == 0 && seconds == 10) {
	    	  mTimeIsBlinking = true;
	    	  m_Handler.postDelayed(mBlinkTimeTask, 200);
	      } else if (minutes > 0 || seconds > 10) {
	    	  if(mTimeIsBlinking) {	    		  
	    		  m_Handler.removeCallbacks(mBlinkTimeTask);
	    		  mPlayTime.setVisibility(View.VISIBLE);
	    	  }
	    	  mTimeIsBlinking = false;
	      }
	   }
    };

    private Runnable mBlinkTimeTask = new Runnable() {
 	   public void run() {	
 		  m_Handler.removeCallbacks(mBlinkTimeTask);
 		  mPlayTime_visibility ^= true; 
 		  mPlayTime.setVisibility(
 				  mPlayTime_visibility ? View.VISIBLE : View.INVISIBLE);
 		  m_Handler.postDelayed(mBlinkTimeTask, 200);
 	   }
    };
    
    private Runnable mDemoPlayTask = new Runnable() {    	
 	   	public void run() { 		   	    		  	   		
 	   		switch(mDemoState) {
 	   			case 0: 				   
				   showMessage("Toque em letras adjacentes\n"
						   + "procurando formar palavras\ncom pelo menos 4 letras.\n"
						   + "Quando terminar uma palavra toque no botão.", 20);
				   m_Handler.postDelayed(this, 5000);
				   ++mDemoState;
				   break; 				
 	   			case 1:
 	   			case 4:
 	   				m_text_status.setVisibility(View.INVISIBLE);
 	   				ArrayList<PlayBoard.Coordinate> word = mBoardGameLogic.getMasterWord();				   
				   	PlayBoard.Coordinate pos ;
				   	String current_word = "";
					for(int i = 0; i < m_demo_word_i; ++i) {
						pos = word.get(i);
						m_playboard.setTile(PlayBoard.SELECTED, 
							 pos.x, pos.y);
						current_word += m_playboard.getTextAtPos(pos.x, pos.y);
					} 						   
					pos = word.get(m_demo_word_i);
					current_word += m_playboard.getTextAtPos(pos.x, pos.y);
					m_playboard.setTile(PlayBoard.LAST_SELECTED, 
							pos.x, pos.y);
					
		        	m_word.setText(current_word);
		        	
		        	int button_img = (current_word.length() > 3) ? R.drawable.question : R.drawable.check_button_blank;
		        	m_word.setCompoundDrawablesWithIntrinsicBounds(0, 0, button_img, 0);
		        	m_word.setEnabled(current_word.length() > 3);
		        	m_word.setClickable(false);
		        						
					m_playboard.invalidate();
					m_Handler.postDelayed(this, 1000);					  
					if(++m_demo_word_i == word.size())
						mDemoState++;
				   break; 	 	   			
 	   			case 2:
 	   				showResult("SABONETE", "certa", 0xAA00FF00);
 	   				m_Handler.postDelayed(this, 3000);
 	   				mDemoState++;
 	   				break;
 	   			case 3:
 	   				m_demo_word_i = 0;
 	   				m_text_status.setVisibility(View.INVISIBLE);
 	   				mBoardGameLogic.startGame("PORTUGAL");
 	   				m_playboard.invalidate();
 	   				m_Handler.postDelayed(this, 1000);
 	   				mDemoState++;
 	   				break;
 	   			case 5:
 	   				showResult("PORTUGAL", "errada", 0xAAFF0000);
 	   				m_Handler.postDelayed(this, 2200);
 	   				mDemoState++;
 	   				break;
 	   			case 6:
 	   				showMessage("Não são aceites nomes próprios.", 20);
 	   				m_Handler.postDelayed(this, 5000);
 	   				mDemoState++;
 	   				break; 	   				
 	   			case 7:
 	   				finish();
 	   			 	   				
 	   		}
 	   	}
     };
        
    private void showResult(String word, String result, int color) {
    	m_text_status.setTextSize(40);
  		m_text_status.setText("Palavra "+result+"\n"+word);
  		m_text_status.setBackgroundColor(color);
    	m_text_status.setVisibility(View.VISIBLE);    	
    	m_Handler.postDelayed(mDismissResultTask, 2000);
    }
    
    private Runnable mDismissResultTask = new Runnable() {
    	public void run() {
    		if(mPlayState == STATE_RUNNING) {
    			m_text_status.setVisibility(View.INVISIBLE);    			
    		}
	 		if(m_must_reset_word) {
	 			mBoardGameLogic.swapSelected();
	 			m_must_reset_word = false;
	 		}
	 		mBoardGameLogic.clearAll();
	 	}
	 };
	 
	 @Override
	 public void onConfigurationChanged(Configuration newConfig) {
	     super.onConfigurationChanged(newConfig);
	     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	 }
	 
	 
	 @Override
	 public void onSaveInstanceState(Bundle savedInstanceState) {
		 // Save UI state changes to the savedInstanceState.
		 // This bundle will be passed to onCreate if the process is
		 // killed and restarted.
	
		super.onSaveInstanceState(savedInstanceState);
	      	
		Log.d(TAG, "onSaveInstanceState()");		
		savedInstanceState.putInt("score", m_score);
		savedInstanceState.putLong("played_time", System.currentTimeMillis() - mStartTime);
		savedInstanceState.putString("board_data", m_playboard.getBoardData());
		savedInstanceState.putInt("playstate", mPlayState);
		savedInstanceState.putBoolean("mTimeIsBlinking", mTimeIsBlinking);
	 }

	 @Override
	 protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		Log.d(TAG, "onRestoreInstanceState() "+savedInstanceState);
		mPlayState = savedInstanceState.getInt("playstate");
		m_score = savedInstanceState.getInt("score"); 
		long played_time = savedInstanceState.getLong("played_time");
		String board_data = savedInstanceState.getString("board_data");
		mTimeIsBlinking = savedInstanceState.getBoolean("mTimeIsBlinking");
		mStartTime = System.currentTimeMillis() - played_time;
		m_playboard.setBoarData(board_data);        	    	
	 }
	 
	 @Override
	 protected void onPause () {
		 super.onPause();	
		 if(!isFinishing())
			 mPlayedTime = System.currentTimeMillis() - mStartTime;	
		 else
			 mPlayedTime = -1;
		 m_Handler.removeCallbacks(mUpdateTimeTask);
		 m_Handler.removeCallbacks(mDemoPlayTask);
		 m_Handler.removeCallbacks(mBlinkTimeTask);
	 }

	 @Override
	 protected void onResume () {
		 super.onResume();		 
		 Log.d(TAG, "onResume()");

		 if(mPlayState == STATE_RUNNING && mPlayedTime > -1) {
			 mStartTime = System.currentTimeMillis() - mPlayedTime;
			 m_Handler.postDelayed(mUpdateTimeTask, 1000);
		 }
		 if(mTimeIsBlinking)
			 m_Handler.postDelayed(mBlinkTimeTask, 200);
	 }	
	 
	 @Override
	 protected void onDestroy () {
		 super.onDestroy();
		 Log.d(TAG, "onDestroy()");		 
		 m_db.close();		 
	 }	 

 
}