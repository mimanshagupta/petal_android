package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/*
 * class that creates the video playlist screen
 */
public class playlistActivity extends Activity { 
    ArrayAdapter<String> adapter;  
    TextView chooseAVid;
    ArrayList<String> link_list = new ArrayList<>();
    ArrayList<String> video_ids = new ArrayList<>();
    ArrayList<String> links = new ArrayList<String>();
    ArrayList<ParseObject> videos = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {//need onCreate to start the activity 
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.listwithtext); 
        //making array adapter with string--> indicates that the array adapter type is a string 
        //[we're working with arrays with strings...]

        adapter = new ArrayAdapter<String>(this,R.layout.list,link_list);//constructor
          
        ListView lv= (ListView) findViewById(R.id.playList); 
        lv.setAdapter(adapter);  
        //getListView().setBackgroundColor(Color.); 
        lv.setOnItemClickListener(new OnItemClickListener(){
  
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, 
                    long arg3) { 
                //Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT).show(); 
                //String absPath= parent.getItemAtPosition(position).toString();
                //System.out.println(absPath);
//                    }
//                }
                ParseObject clicked = videos.get(position);
                int w = clicked.getInt("watched");
                w++;
                clicked.put("watched", w);
                clicked.saveInBackground();
                Intent intent = new Intent();//(playlistActivity.this, VideoActivity.class); 
                intent.setClass(getApplicationContext(), DummyClass.class); //changed this from VideoActivity.class; this instead of getApplicationContext()? 
                intent.putExtra("video", links.get(position));
                intent.putExtra("video_id", video_ids.get(position));


                startActivity(intent); 
                  
            } 
              
        }); 
        //lv.setBackgroundResource(R.drawable.backgroundblank); 
        lv.setCacheColorHint(Color.TRANSPARENT); 
        lv.setOverScrollMode(View.OVER_SCROLL_NEVER); 
        lv.setSelector(R.drawable.listviewselector); 
        getVideos();
        Typeface font= Typeface.createFromAsset(this.getAssets(),"ArchitectsDaughter.ttf"); 
        chooseAVid= (TextView) findViewById(R.id.aTextView); 
        chooseAVid.setText("Choose A Video"); 
        chooseAVid.setTypeface(font); 
    }
      
    public void getVideos(){ //name of the method to get Videos

        final ArrayList<String> titles = new ArrayList<String>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Videos");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects == null) {
                        Log.e("mushuball", "seriously ???");
                    } else {
                        Log.e("mushuball", "Retrieved " + objects.size() + " videos");
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject vids = objects.get(i);
                            //Log.e("mushuball", vids.getString("vid_title") + " " + vids.getString("vid_link"));
                            titles.add(vids.getString("vid_title"));
                            video_ids.add(vids.getObjectId());
                            videos.add(vids);
                            links.add(vids.getString("vid_link"));
                        }
                        addVideos(titles);
                    }

                } else {
                    Log.e("mushuball", "Error: " + e.getMessage());
                }
            }
        });
    }


    public void addVideos(ArrayList<String> a) {
        for(String s : a) {
            link_list.add(s);
        }
        adapter.notifyDataSetChanged();
    }
      
  
      
} 