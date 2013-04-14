package com.bluehsoft.papaletras.free;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MenuActivity extends Activity {

	private static final String MY_AD_UNIT_ID = "a14db692fdab56d";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
     	setContentView(R.layout.menu);

        // Create the adView
        AdView adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
        // Lookup your LinearLayout assuming it’s been given
        // the attribute android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout)findViewById(R.id.menu_layout);
        // Add the adView to it
        layout.addView(adView);

        // Initiate a generic request to load it with an ad
        AdRequest request = new AdRequest();
        request.setTesting(true); 
        //adView.setAdListener(this);
        adView.loadAd(request);      
        
     	//TextView link = (TextView) findViewById(R.id.link);
     	//Linkify.addLinks(link, Linkify.ALL);
    }
    
	public void startGameOnClick(View view) {
		Intent myIntent = new Intent(MenuActivity.this, DatabaseInitActivity.class);
		myIntent.putExtra("mode", "start");
		startActivity(myIntent);		
	}

	public void how_to_playOnClick(View view) {
		Intent myIntent = new Intent(MenuActivity.this, DatabaseInitActivity.class);
		myIntent.putExtra("mode", "demo");
		startActivity(myIntent);		
	}	

	public void HighScores_OnClick(View view) {
		Intent myIntent = new Intent(MenuActivity.this, ScoreShowActivity.class);
		startActivity(myIntent);		
	}	
	
		
}
