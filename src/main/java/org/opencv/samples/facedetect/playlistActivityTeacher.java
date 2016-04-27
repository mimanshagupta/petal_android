package org.opencv.samples.facedetect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * class that creates the video playlist screen
 */
public class playlistActivityTeacher extends Activity {
    ArrayAdapter<String> adapter;  
    TextView chooseAVid;
    Button butNext;
    EditText category, question, option1, option2, option3, answer, minute;
    int counter = 0 ;
    @Override
    public void onCreate(Bundle savedInstanceState) {//need onCreate to start the activity 
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.listwithtextteacher);
        //making array adapter with string--> indicates that the array adapter type is a string 
        //[we're working with arrays with strings...] 
        adapter = new ArrayAdapter<String>(this,R.layout.list,getVideos());//constructor 
          
          
        ListView lv= (ListView) findViewById(R.id.playList); 
        lv.setAdapter(adapter);  
        //getListView().setBackgroundColor(Color.); 
        lv.setOnItemClickListener(new OnItemClickListener(){ 
  
  
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, 
                    long arg3) { 
                //Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT).show(); 
                String absPath= parent.getItemAtPosition(position).toString();
                final String videoTitle = absPath.split("/")[absPath.split("/").length-1];

                AlertDialog.Builder quiz = new AlertDialog.Builder(playlistActivityTeacher.this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.enter_questions, null);
                category = (EditText) dialogView.findViewById(R.id.category);
                butNext = (Button) dialogView.findViewById(R.id.button_next);
                question = (EditText) dialogView.findViewById(R.id.question);
                option1 = (EditText) dialogView.findViewById(R.id.option1);
                option2 = (EditText) dialogView.findViewById(R.id.option2);
                option3 = (EditText) dialogView.findViewById(R.id.option3);
                answer = (EditText) dialogView.findViewById(R.id.answer);
                minute = (EditText) dialogView.findViewById(R.id.minute);
                quiz.setTitle("Mini Quiz");
                quiz.setNeutralButton("Finish", null);
                butNext.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View view) {
                        String c = category.getText().toString();
                        String q = question.getText().toString();
                        String o1 = option1.getText().toString();
                        String o2 = option2.getText().toString();
                        String o3 = option3.getText().toString();
                        String a = answer.getText().toString();
                        String m = minute.getText().toString();

                        int seconds = cnvert2second(m);

                        ParseObject testObject = new ParseObject("Questions");
                        testObject.put("Video", videoTitle);
                        testObject.put("Category", c);
                        testObject.put("Question", q);
                        testObject.put("Opt1", o1);
                        testObject.put("Opt2", o2);
                        testObject.put("Opt3", o3);
                        testObject.put("Answer", a);
                        testObject.put("Time", seconds);
                        testObject.saveInBackground();
                        counter ++ ;
                        Toast.makeText(getBaseContext(), counter + " questions saved", Toast.LENGTH_SHORT).show();

                        category.getText().clear();
                        question.getText().clear();
                        option1.getText().clear();
                        option2.getText().clear();
                        option3.getText().clear();
                        answer.getText().clear();
                        minute.getText().clear();
                    }
                });
                quiz.setView(dialogView);
                quiz.show();
                  
            } 
              
        }); 
        //lv.setBackgroundResource(R.drawable.backgroundblank); 
        lv.setCacheColorHint(Color.TRANSPARENT); 
        lv.setOverScrollMode(View.OVER_SCROLL_NEVER); 
        lv.setSelector(R.drawable.listviewselector); 
          
        Typeface font= Typeface.createFromAsset(this.getAssets(),"ArchitectsDaughter.ttf"); 
        chooseAVid= (TextView) findViewById(R.id.aTextView); 
        chooseAVid.setText("Choose A Video"); 
        chooseAVid.setTypeface(font); 
    }

    public int cnvert2second(String m){
        int hh = Integer.parseInt(m.split(":")[0]);
        int mm = Integer.parseInt(m.split(":")[1]);
        int ss = Integer.parseInt(m.split(":")[2]);

        return hh*3600 + mm*60 + ss;
    }
      
    static String[] mVids = null; //"initiating" the list of strings of videos 
      
    public static String[] getVideos(){ //name of the method to get Videos
        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> links = new ArrayList<String>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Videos");
        query.findInBackground(new FindCallback<ParseObject>(){
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject vids : objects) {
                    titles.add(vids.getString("vid_title"));
                    links.add(vids.getString("vid_link"));
                }
            }
        });
  
//        File videos= Environment.getExternalStorageDirectory(); //get all the uh files on the device
//        File something= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//        File[] pushedList= videos.listFiles(new FilenameFilter(){ //vidList accepts files with a certain restriction
//                                            @Override
//                                            public boolean accept(File dir, String name){
//                                            return ((name.endsWith(".3gp")||(name.endsWith(".mp4"))));//||(name.endsWith(".webm")))));
//            }
//
//        });
//        File[] alreadyList=something.listFiles(new FilenameFilter(){ //vidList accepts files with a certain restriction
//            @Override
//            public boolean accept(File dir, String name){
//            return ((name.endsWith(".3gp")||(name.endsWith(".mp4"))));//||(name.endsWith(".webm")))));
//            }
//        });
//
//
//        ArrayList<File> alist1= new ArrayList<File>(Arrays.asList(pushedList));
//        ArrayList<File> alist2= new ArrayList<File>(Arrays.asList(alreadyList));
//        alist1.addAll(alist2);
  
          
        //File[] vidList= (File[]) alist1.toArray(new File[0]);
        mVids= new String[links.size()]; //final vid list will be the length of vidList
        for (int i=0; i<links.size(); i++){
            mVids[i]= links.get(i); //fill mVids with paths for each vidList's valid files
        } 
          
        return mVids; 
    } 
      
      
  
      
} 