package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * this is the screen right before calibration (the one with the button that you must click to then start calibration)
 */
public class MainScreen extends Activity {
	Button start; 
	Intent camIntent= new Intent(); 
	TextView explanation; 
	ImageView background; 
	AnimationDrawable animation; 
	private Handler handler = new Handler(); 
	private Runnable unbold; 
	Typeface font; 

	@Override
	public void onCreate(Bundle savedInstanceState){ 
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.menu); 

		background= (ImageView) findViewById(R.id.imagehost); 
		background.setBackgroundResource(R.drawable.anim); 
		animation= (AnimationDrawable) background.getBackground(); 
		font= Typeface.createFromAsset(this.getAssets(),"ArchitectsDaughter.ttf"); 

		start= (Button) findViewById(R.id.camButt); 
		start.setTypeface(font); 

		explanation= (TextView) findViewById(R.id.textView1); 
		explanation.setTypeface(font); 

		start.setOnClickListener(new OnClickListener(){ 
			public void onClick(View v){ 

				unbold= new Runnable(){ 
					public void run(){ 
						start.setTypeface(font, Typeface.NORMAL); 
					} 
				}; 
				start.setTypeface(font, Typeface.BOLD); 

				handler.postDelayed(unbold, 20); 
				camIntent.setClass(getApplicationContext(), Calibration.class);  
				camIntent.setAction(Intent.ACTION_CALL); 
				startActivity(camIntent);  
			} 
		});
	}  

	@Override
	public void onWindowFocusChanged(boolean hasFocus){ 
		super.onWindowFocusChanged(hasFocus); 
		animation.start();
	} 
}
