package org.opencv.samples.facedetect; 
  
  
import android.app.Activity; 
import android.content.Context; 
import android.content.Intent; 
import android.graphics.Typeface; 
import android.os.Bundle; 
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater; 
import android.view.View; 
import android.view.ViewGroup; 
import android.view.ViewGroup.LayoutParams; 
import android.view.WindowManager; 
import android.widget.Button; 
import android.widget.PopupWindow; 
import android.widget.RelativeLayout; 
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.parse.ParseObject;


public class Menu2 extends Activity { 
  
    private Button startButton, instructionsPopup, aboutPopup, aboutClosePopup, instructionsClosePopup, teacherButton;
    private PopupWindow instructionsPopupWindow, aboutPopupWindow; 
  
    private Typeface font; 
    private boolean click; 
    private Handler handler = new Handler(); 
    private Handler handler2 = new Handler(); 
    private Runnable unbold, unbold2; 
    private RelativeLayout menu_layout; 
    private TextView titletext, titletext2,desctext, desctext2; 
//  static ViewHolder holder; 
    @Override
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.menuscreen);
  
        startButton = (Button) findViewById(R.id.start);
        teacherButton = (Button) findViewById(R.id.teacher);
        instructionsPopup= (Button)findViewById(R.id.instructionsButton);  
        aboutPopup = (Button)findViewById(R.id.aboutButton);  
        font = Typeface.createFromAsset(this.getAssets(),"ArchitectsDaughter.ttf"); 
        startButton.setTypeface(font);
        teacherButton.setTypeface(font);
        instructionsPopup.setTypeface(font); 
        aboutPopup.setTypeface(font);

        Firebase ref = new Firebase("https://burning-torch-1326.firebaseio.com");

        Firebase aref = ref.child("test");
        aref.child("fullName").setValue("Alan Turing", new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e("mushuball", "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.e("mushuball", "Data saved successfully.");
                }
            }
        });
        aref.child("birthYear").setValue(1912, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e("mushuball", "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.e("mushuball", "Data saved successfully.");
                }
            }
        });
    } 
  
    public void onClick(View v) { 
  
        switch(v.getId()) { 
        case R.id.start: 
              
            unbold= new Runnable(){ 
                public void run(){ 
                    startButton.setTypeface(font, Typeface.NORMAL); 
                } 
            }; 
            startButton.setTypeface(font, Typeface.BOLD); 
  
            handler.postDelayed(unbold, 20); 
  
  
            Intent intent = new Intent(Menu2.this, StudentLoginActivity.class);
            startActivity(intent); 
              
            break; 
        case R.id.aboutButton: 
            unbold= new Runnable(){ 
                public void run(){ 
                    aboutPopup.setTypeface(font, Typeface.NORMAL); 
                } 
            }; 
            aboutPopup.setTypeface(font, Typeface.BOLD); 
  
            handler.postDelayed(unbold, 100); 
              
            unbold2 =new Runnable(){ 
                public void run(){ 
                    initiatePopupWindow(R.layout.about_description, R.id.popup_element_about, R.id.aboutButton);  
  
                } 
            }; 
              
            handler2.postDelayed(unbold2, 300); 
            break; 
        case R.id.instructionsButton: 
            unbold= new Runnable(){ 
                public void run(){ 
                    instructionsPopup.setTypeface(font, Typeface.NORMAL); 
                } 
            }; 
            instructionsPopup.setTypeface(font, Typeface.BOLD); 
  
            handler.postDelayed(unbold, 100); 
              
            unbold2 =new Runnable(){ 
                public void run(){ 
                    initiatePopupWindow(R.layout.instructions_description, R.id.popup_element, R.id.instructionsButton);   
  
                } 
            }; 
              
            handler2.postDelayed(unbold2, 300); 
              
            break;
            case R.id.teacher:
                unbold= new Runnable(){
                    public void run(){
                        teacherButton.setTypeface(font, Typeface.NORMAL);
                    }
                };
                teacherButton.setTypeface(font, Typeface.BOLD);

                handler.postDelayed(unbold, 20);

                Intent intent2 = new Intent(Menu2.this, TeacherLoginActivity.class);
                startActivity(intent2);

                break;
        } 
  
    } 
  
    public void closeOnClick(View v){  
        switch(v.getId()){  
        case R.id.close_instruct_popup:  
            instructionsPopupWindow.dismiss();  
            startButton.setVisibility(View.VISIBLE); 
            aboutPopup.setVisibility(View.VISIBLE); 
            instructionsPopup.setVisibility(View.VISIBLE); 
            break;  
        case R.id.close_about_popup:  
            aboutPopupWindow.dismiss();  
            startButton.setVisibility(View.VISIBLE); 
            aboutPopup.setVisibility(View.VISIBLE); 
            instructionsPopup.setVisibility(View.VISIBLE); 
            break;  
        }  
    } 
      
    private void initiatePopupWindow(int description, int popup_element, int buttonid){  
        try{  
            LayoutInflater inflater = (LayoutInflater) Menu2.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            View layout = inflater.inflate(description,  
                    (ViewGroup) findViewById(popup_element));  
            if(buttonid==R.id.aboutButton){  
                startButton.setVisibility(View.GONE); 
                aboutPopup.setVisibility(View.GONE); 
                instructionsPopup.setVisibility(View.GONE); 
                  
                  
                aboutPopupWindow = new PopupWindow(layout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);   
                aboutPopupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);       
                  
                  
                aboutClosePopup = (Button) layout.findViewById(R.id.close_about_popup); 
                System.out.println(aboutClosePopup); 
                aboutClosePopup.setTypeface(font); 
                  
                titletext = (TextView) aboutPopupWindow.getContentView().findViewById(R.id.abouttitle); 
                System.out.println(titletext); 
                titletext.setTypeface(font); 
                desctext = (TextView) aboutPopupWindow.getContentView().findViewById(R.id.aboutdescription); 
                desctext.setTypeface(font); 
  
            }  
            else{ 
                startButton.setVisibility(View.GONE); 
                aboutPopup.setVisibility(View.GONE); 
                instructionsPopup.setVisibility(View.GONE); 
                  
                instructionsPopupWindow = new PopupWindow(layout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);   
                instructionsPopupWindow.showAtLocation(layout, Gravity.TOP, 0, 0); 
                  
                instructionsClosePopup = (Button) layout.findViewById(R.id.close_instruct_popup); 
                instructionsClosePopup.setTypeface(font); 
                  
                titletext2 = (TextView) instructionsPopupWindow.getContentView().findViewById(R.id.ititle); 
                titletext2.setTypeface(font); 
                desctext2 = (TextView) instructionsPopupWindow.getContentView().findViewById(R.id.idesc); 
                desctext2.setTypeface(font); 
                  
                  
            }  
                
        } catch (Exception e) {   
            e.printStackTrace();   
        }   
    } 
  
  
} 