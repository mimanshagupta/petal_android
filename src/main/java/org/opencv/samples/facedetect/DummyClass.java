package org.opencv.samples.facedetect;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
/*
 * DummyClass is used as the container for the two fragments (VidFrag and FragFD)
 */
public class DummyClass extends android.support.v4.app.FragmentActivity{
	BroadcastReceiver mReceiver;
	
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		IntentFilter filter= new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new screenLock();
		registerReceiver(mReceiver, filter);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		setContentView(R.layout.dummylayout);
	}
	  @Override
	    protected void onPause() {
	        // WHEN THE SCREEN IS ABOUT TO TURN OFF
	        if (screenLock.wasScreenOn) {
	            // THIS IS THE CASE WHEN ONPAUSE() IS CALLED BY THE SYSTEM DUE TO A SCREEN STATE CHANGE
	        } else {
	            // THIS IS WHEN ONPAUSE() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED
	        }
	        super.onPause();
	    }
	 
	    @Override
	    protected void onResume() {
	        // ONLY WHEN SCREEN TURNS ON
	        if (!screenLock.wasScreenOn) {
	            // THIS IS WHEN ONRESUME() IS CALLED DUE TO A SCREEN STATE CHANGE
	        } else {
	            // THIS IS WHEN ONRESUME() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED
	        }
	        super.onResume();
	    }
	    
	    @Override
	    public void onDestroy(){
	    	super.onDestroy();
	    	unregisterReceiver(mReceiver);
	    }
}
