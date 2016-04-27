package org.opencv.samples.facedetect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/*
 * creates video fragment
 */
public class VidFrag extends Fragment implements SurfaceHolder.Callback,MediaPlayerControl, OnCompletionListener, View.OnClickListener{
    static ControlVid vidPlayer;
    private final android.os.Handler mHandler = new Handler();
    public static boolean quizTime = false;
    private boolean killMe = true;
    int numOfQuestions = 0;
    float score = 0f;
    Question currentQ = null;
    TextView txtQuestion;
    RadioButton rda, rdb, rdc;
    Button butNext;
    List<ParseObject> allQuestions = new ArrayList<>();
    List<ParseObject> quizEntries = new ArrayList<>();
    List<Integer> times = new ArrayList<>();
    List<Integer> difficulties = new ArrayList<>();
    boolean[] hasAppeared;
    int previous_time = 0;
    boolean prev_correct = false;
    @Override
    public int getAudioSessionId() {
        return 0;
    }

    MediaController buttons;
    Button vidButton;
    Button aButton;
    Handler myHandler= new Handler();
    Timer timer= new Timer();
    Method mGetRawW;
    Method mGetRawH;
    static int distrVar;
    Intent endIntent;
    public static boolean end= false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        end= false;
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        View view= inflater.inflate(R.layout.video, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getActivity().getIntent().getExtras();
        final String path= bundle.getString("video");
        final String videoId= bundle.getString("video_id");
        vidPlayer= (ControlVid) getView().findViewById(R.id.vidPlayer);
        vidPlayer.setKeepScreenOn(true);
        vidPlayer.setPlayPauseListener(new ControlVid.PlayPauseListener() {

            @Override
            public void onPlay() {
            }

            @Override
            public void onPause() {
                distrVar = vidPlayer.getCurrentPosition();
            }
        });
        //media controller layout
        buttons= new MediaController(getActivity());
        buttons.setMediaPlayer(vidPlayer);
        buttons.setAnchorView(vidPlayer); //initializes its hovering/disappearing features
        //        //actual video player video/final video-y initializing things

        Uri video= Uri.parse(path); //vidPlayer uses this path to play videos
        vidPlayer.setVideoURI(video);
        vidPlayer.setMediaController(buttons);
        vidPlayer.setVisibility(View.VISIBLE);
        vidPlayer.setZOrderOnTop(false);
        vidPlayer.requestFocus();
        vidPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                //killRunnable();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent main = new Intent(getActivity().getApplicationContext(), LastScreen.class);
                        main.putExtra("video_id", videoId);
                        getActivity().startActivity(main);
                        getActivity().finish();
                    }
                });
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Quiz");
        query.whereEqualTo("videoId", videoId);
        query.addAscendingOrder("timeStamp");
        query.addDescendingOrder("difficulty");
        try {
            List<ParseObject> objects = query.find();
            for(ParseObject object : objects) {
                quizEntries.add(object);
                ParseQuery<ParseObject> q = ParseQuery.getQuery("Questions");
                q.whereEqualTo("objectId", object.getString("questionId"));
                ParseObject qq = q.getFirst();
                Log.e("mushuball", "timestamp = " + object.getNumber("timeStamp") + ", difficulty = " + object.getNumber("difficulty") + ", question = " + qq.getString("Question"));
                allQuestions.add(qq);
                times.add(object.getInt("timeStamp"));
                //Collections.sort(times);

                difficulties.add(object.getInt("difficulty"));
            }
            vidPlayer.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Runnable mRunnable = new Runnable() {
            public void run() {

                final int currentPosition = vidPlayer.getCurrentPosition()/1000;

                if(times.indexOf(currentPosition) != -1 && previous_time != currentPosition && previous_time < currentPosition && !quizTime) {
                    unKillRunnable();
                }

                if (!killMe) {
                    score = 0;
                    //currentQ = null
                    chooseQuestion(currentPosition, prev_correct);
                    //currentQ is fist question
                    quizTime = true;
                    killRunnable();
                    vidPlayer.pause();

                    final AlertDialog.Builder quiz = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.activity_quiz, null);
                    quiz.setTitle("Mini Quiz");
                    quiz.setView(dialogView);

                    txtQuestion = (TextView) dialogView.findViewById(R.id.textView1);
                    rda = (RadioButton) dialogView.findViewById(R.id.radio0);
                    rdb = (RadioButton) dialogView.findViewById(R.id.radio1);
                    rdc = (RadioButton) dialogView.findViewById(R.id.radio2);
                    butNext = (Button) dialogView.findViewById(R.id.button1);

                    setQuestionView();
                    previous_time = currentPosition;

                    final AlertDialog dialog = quiz.create();

                    butNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RadioGroup grp = (RadioGroup) dialogView.findViewById(R.id.radioGroup1);
                            RadioButton answer = (RadioButton) dialogView.findViewById(grp.getCheckedRadioButtonId());

                            Log.d("yourans", currentQ.getANSWER() + " " + answer.getText());

                            for(ParseObject entry: quizEntries) {
                                if(entry.getString("questionId").equals(currentQ.getID())){
                                    if (answer.getText().equals(currentQ.getOPTA())) {
                                        int a = entry.getInt("answeredA");
                                        entry.put("answeredA", a+1);
                                    } else if (answer.getText().equals(currentQ.getOPTB())) {
                                        int b = entry.getInt("answeredB");
                                        entry.put("answeredB", b+1);
                                    } else if (answer.getText().equals(currentQ.getOPTC())) {
                                        int c = entry.getInt("answeredC");
                                        entry.put("answeredC", c+1);
                                    }
                                    entry.saveInBackground();
                                }
                            }

                            if (currentQ.getANSWER().equals(answer.getText())) {
                                prev_correct = true;
                                score++;
                            } else {
                                prev_correct = false;
                            }
                            Log.e("mushuball", "Your score " + score);
                            chooseQuestion(currentPosition, prev_correct);
                            //currentQ might be null

                            if (currentQ != null) {
                                setQuestionView();
                            } else {
                                butNext.setText("Finish");
                                butNext.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        AlertDialog.Builder result = new AlertDialog.Builder(getActivity());
                                        result.setTitle("Your Result");
                                        result.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                killRunnable();
                                                quizTime = false;
                                                vidResume();
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        final View dialogView2 = inflater.inflate(R.layout.activity_result, null);
                                        result.setView(dialogView2);
                                        float finalScore = (score / numOfQuestions) * 5f;
                                        RatingBar bar = (RatingBar) dialogView2.findViewById(R.id.ratingBar1);
                                        bar.setNumStars(5);
                                        bar.setStepSize(0.5f);
                                        //get text view
                                        TextView t = (TextView) dialogView2.findViewById(R.id.textResult);
                                        //display score
                                        Log.e("mushuball", "final score is: " + finalScore);
                                        int tempScore = Math.round(finalScore);
                                        Log.e("mushuball", "temp score is: " + tempScore);
                                        bar.setRating(tempScore);
                                        switch (tempScore) {
                                            case 0:
                                                t.setText("I think you need to restart the video");
                                                break;
                                            case 1:
                                                t.setText("I think you need to pay more attention to the video");
                                                break;
                                            case 2:
                                                t.setText("I think you need to pay more attention to the video");
                                                break;
                                            case 3:
                                                t.setText("Not bad, kid!");
                                                break;
                                            case 4:
                                                t.setText("Not bad, kid!");
                                                break;
                                            case 5:
                                                t.setText("Great job, slugger!");
                                                break;
                                        }
                                        result.show();
                                    }
                                });
                            }
                        }
                    });
                    dialog.show();
                }
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    public void chooseQuestion(int current_time, boolean correct) {
        List<Question> temp = new ArrayList<>();
        int index=0;
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) == current_time) {
                ParseObject p = allQuestions.get(i);
                temp.add(new Question(p.getString("Question"), p.getString("Opt1"), p.getString("Opt2"), p.getString("Opt3"), p.getString("Answer"), p.getObjectId()));
                if(currentQ != null) {
                    if(currentQ.getQUESTION().equals(p.getString("Question"))){
                        index = temp.size()-1;
                    }
                }
            }
        }
        numOfQuestions = temp.size();
        if(previous_time != current_time) {
            hasAppeared = new boolean[temp.size()];
        }

        if (currentQ == null) {
            index = (int) Math.floor(temp.size()/2.0);
        }
        else {
            int t = index;
            if(correct) {
                index++;
                index %= temp.size();
                while(hasAppeared[index]) {
                    if (index == t) {
                        currentQ = null;
                        return;
                    }
                    index++;
                    index %= temp.size();
                }
            } else {
                //Log.e("mushuball", "outside index: " + index);
                index--;
                index += temp.size();
                index %= temp.size();
                while(hasAppeared[index]) {
                    //Log.e("mushuball", "inside index: " + index);
                    if (index == t) {
                        currentQ = null;
                        return;
                    }
                    index --;
                    index += temp.size();
                    index %= temp.size();
                }
            }

        }
        Question q = temp.get(index);
        hasAppeared[index] = true;
        currentQ = q;
    }

    private void killRunnable() {
        killMe = true;
    }

    private void unKillRunnable() {
        killMe = false;
    }

    private void setQuestionView()
    {
        txtQuestion.setText(currentQ.getQUESTION());
        rda.setText(currentQ.getOPTA());
        rdb.setText(currentQ.getOPTB());
        rdc.setText(currentQ.getOPTC());
    }

    public static void vidPause(){
        vidPlayer.pause();
    }

    public static void vidSeek(){
        int current= vidPlayer.getCurrentPosition();
        vidPlayer.seekTo(current-5000);
        vidPlayer.start();
    }

    public static void vidResume(){
        vidPlayer.seekTo(distrVar);
        vidPlayer.start();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        int percent= (vidPlayer.getCurrentPosition() * 100)/vidPlayer.getDuration();
        return percent;
    }

    @Override
    public int getCurrentPosition() {
        return vidPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return vidPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return vidPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if (vidPlayer.isPlaying()){
            vidPlayer.pause();
        }
    }

    @Override
    public void seekTo(int pos) {
        vidPlayer.seekTo(pos);

    }

    @Override
    public void start() {
        vidPlayer.start();

    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onResume(){
        super.onResume();
        getActivity().setRequestedOrientation(0);
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        killRunnable();
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run(){
                Intent main= new Intent(getActivity().getApplicationContext(), LastScreen.class);
                getActivity().startActivity(main);
                getActivity().finish();
            }
        });
        getActivity().finish();
    }


    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onLayoutClicked(View v) {
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        vidPlayer.pause();
    }

    @Override
    public void onClick(View view) {

    }
}


