package org.opencv.samples.facedetect; 
  
import android.app.Activity; 
import android.content.Context;
import android.content.Intent; 
import android.content.SharedPreferences;
import android.graphics.Color; 
import android.graphics.Typeface; 
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener; 
import android.widget.ArrayAdapter; 
import android.widget.Button; 
import android.widget.ListAdapter; 
import android.widget.ListView; 
import android.widget.TextView;

import com.parse.ParseObject;

/*
 * displays the last screen with all of the data on it
 */
public class LastScreen extends Activity{ 
    ListView data; 
    Intent finalIntent= new Intent(); 
    Intent finalIntent2= new Intent(); 
    @Override
    public void onCreate(Bundle savedInstanceState){ 
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        
        SharedPreferences something2= this.getSharedPreferences("something2",Context.MODE_PRIVATE);
		int counter_distracted = something2.getInt("counter_distracted", 0);
		int counter_sleepy = something2.getInt("counter_sleepy", 0);
		int counter_zoned = something2.getInt("counter_zoned", 0);

        final String videoId= bundle.getString("video_id");
		
        setContentView(R.layout.lastscreen); 
        data=(ListView) findViewById(R.id.lastList); 
        Typeface font= Typeface.createFromAsset(this.getAssets(),"ArchitectsDaughter.ttf"); 

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_emotionlist, FragFD.emotionList); 
        data.setAdapter(adapter); 
        data.setClickable(false); 
        data.setCacheColorHint(Color.TRANSPARENT); 
        data.setSelector(R.drawable.listviewselector);

        int allEmotions = adapter.getCount();
        for(int i = 0; i < allEmotions; i++) {
            String s = adapter.getItem(i);
            String[] splitS = new String[3];
            splitS = s.split(" ");

            String cause = splitS[2].toLowerCase();
            int timestamp = Integer.parseInt(splitS[0].split("min")[0]) * 60 + Integer.parseInt(splitS[1].split("sec")[0]);

            ParseObject attentiveness = new ParseObject("Attentiveness");

            attentiveness.put("videoId", videoId);
            attentiveness.put("cause", cause);
            attentiveness.put("timestamp", timestamp);
            attentiveness.saveInBackground();
        }
          
        Button calibration = (Button) findViewById(R.id.button1); 
        calibration.setTypeface(font); 
        calibration.setTextSize(25); 
  
        Button playlist= (Button) findViewById(R.id.button2); 
        playlist.setTypeface(font); 
        playlist.setTextSize(25); 
          
        calibration.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finalIntent.setClass(getApplicationContext(), Calibration.class);
                finalIntent.setAction(Intent.ACTION_CALL);
                startActivity(finalIntent);
            }
        }); 
          
        playlist.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finalIntent2.setClass(getApplicationContext(), playlistActivity.class);
                finalIntent2.setAction(Intent.ACTION_CALL);
                startActivity(finalIntent2);
            }
        });

        /*for(int i = 0; i < 3; i++) {
            if(counter_distracted != 0) {
                ParseObject distracted = new ParseObject("Attentiveness");

                distracted.put("videoId", videoId);
                distracted.put("cause", "distracted");
                distracted.put("cheatMode", false);
                distracted.saveInBackground();
            }
        }*/

        TextView finalCount = (TextView)findViewById(R.id.finalCount);
        finalCount.setTypeface(font);
        finalCount.setTextSize(23);
        
        TextView distracted = (TextView)findViewById(R.id.counter_distracted);
        distracted.setTypeface(font);
        distracted.setTextSize(20);
        distracted.setText("DISTRACTED: " + counter_distracted + "   ");
        
        TextView sleepy = (TextView)findViewById(R.id.counter_sleepy);
        sleepy.setTypeface(font);
        sleepy.setTextSize(20);
        sleepy.setText("SLEEPY: "+ counter_sleepy + "   ");
        
        TextView zoned = (TextView)findViewById(R.id.counter_zoned);
        zoned.setTypeface(font);
        zoned.setTextSize(20);
        zoned.setText("ZONED OUT: " + counter_zoned + "   ");
    }  
      
    @Override
    public void onDestroy(){ 
        super.onDestroy(); 
    } 
} 