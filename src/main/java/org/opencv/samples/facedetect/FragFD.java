package org.opencv.samples.facedetect;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.SharedPreferences;
/*
 * This class performs the actual emotion detection that occurs as the user watches the video and the camera streams live data
 */
public class FragFD extends Fragment implements CvCameraViewListener2{
	private static final String    TAG                 = "OCVSample::Activity";
	private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
	public static final int        JAVA_DETECTOR       = 0;
	public static final int        NATIVE_DETECTOR     = 1;

	private Mat                    mRgba;
	private Mat                    mGray;


	private File                   mCascadeFile,mCascadeFileEye;
	private CascadeClassifier      mJavaDetector,mEyeDetector;
	private DetectionBasedTracker  mNativeDetector, mEyeNativeDetector;

	private int                    mDetectorType       = JAVA_DETECTOR;
	private String[]               mDetectorName;
	private Rect[] 				   eyesArray;

	private float                  mRelativeFaceSize   = 0.2f;
	private int                    mAbsoluteFaceSize   = 0;

	private Rect eyearea_left,eyearea_right = new Rect();

	private CameraBridgeViewBase   mOpenCvCameraView;
	private FrameLayout prev;
	private int[] eyeLocArray1 = new int[5];
	private int[] eyeLocArray2 = new int[5];
	private int distracted_counter = 0;
	private static int faceCounter = 0;
	private static int DISTRACTED = 0;
	private static int DISTRACTED_EYE_SHIFT = 1;
	private static int DEFAULT = -1;
	private static int SLEEPY = 2;
	private static int ZONEOUT = 3;
	private int emotion= DEFAULT;
	private boolean noEyes = false;
	public boolean tooClose;
	private int ReyeRcornerFinal;
	private int LeyeRcornerFinal;
	private int ReyeLcornerFinal;
	private int LeyeLcornerFinal;
	private static int eyeCornerCounter = 0;
	public static ArrayList<String> emotionList = new ArrayList<String>();
	private static ArrayList<Integer> emotionListNumber = new ArrayList<Integer>();
	private int counter_distracted=0;
	private int counter_zoned=0;
	private int counter_sleepy=0;
	public static boolean fdIsOn= true;//keeps track of if face detection is running

	public boolean timerOn;
	private Mat leftEyeROI;
	private boolean previous;
	public boolean sleeping;
	private Rect eyearea_left2;
	private double zoomFactor = 1.3;
	private static double time;
	Handler delayAgain= new Handler(); 
	Runnable delayFD; 
	

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(getActivity()) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("detection_based_tracker");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getActivity().getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					InputStream isEye = getResources().openRawResource(R.raw.haarcascade_eye);
					File eyecascadeDir = getActivity().getDir("cascadeEye", Context.MODE_PRIVATE);
					mCascadeFileEye = new File(eyecascadeDir, "haarcascade_eye.xml");
					FileOutputStream osEye = new FileOutputStream(mCascadeFileEye);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					while ((bytesRead = isEye.read(buffer)) != -1) {
						osEye.write(buffer, 0, bytesRead);
					}
					isEye.close();
					osEye.close();


					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

					mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

					cascadeDir.delete();


					mEyeDetector = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
					if (mEyeDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mEyeDetector = null;
					} else
						mEyeNativeDetector = new DetectionBasedTracker(mCascadeFileEye.getAbsolutePath(), 0);
					eyecascadeDir.delete();


				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public FragFD() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	public void onAttach(FragmentActivity VidFrag){
		super.onAttach(VidFrag);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		Log.i(TAG, "called onCreate");
		super.onCreateView(inflater, container,savedInstanceState);
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		View view= inflater.inflate(R.layout.camera, container, false);
		return view;
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();		
		//		SharedPreferences something= getActivity().getSharedPreferences("something",Context.MODE_PRIVATE);
		//		SharedPreferences.Editor editor= something.edit();
		//		editor.clear();
		//		editor.commit();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mOpenCvCameraView = (CameraBridgeViewBase) getView().findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setZOrderOnTop(true);
		mOpenCvCameraView.setVisibility(View.VISIBLE);
		fdIsOn= true;

		//hiding the camera preview
		prev = (FrameLayout)getView().findViewById(R.id.frame_layout);
		((ViewGroup)mOpenCvCameraView.getParent()).removeView(mOpenCvCameraView);
		prev.addView(mOpenCvCameraView, new ViewGroup.LayoutParams(1,1));

		mOpenCvCameraView.setZOrderMediaOverlay(true);
	}

	@Override
	public void onPause(){
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume(){
		super.onResume(); 
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, getActivity(), mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
		
		
	}

	public void tallyEmotionList(){
		for (int i = 0; i < emotionListNumber.size(); i++){
			if(emotionListNumber.get(i)==DISTRACTED){
				counter_distracted++;
			}
			else if(emotionListNumber.get(i)==SLEEPY){
				counter_sleepy++;
			}
			else{//==ZONEOUT
				counter_zoned++;
			}
		}
	}
	public void onCameraViewStarted(int width, int height) {
		emotionList.clear(); 
		emotionListNumber.clear();
		mGray = new Mat();
		mRgba = new Mat();
	}

	public String arrayToString(int[] array){
		String s = "";
		for (int i = 0; i < array.length; i++){
			s= s + array[i] + ", ";
		}
		return s;
	}

	public void onCameraViewStopped() {
		mOpenCvCameraView.disableView();
		mGray.release();
		mRgba.release();
		tallyEmotionList();
		SharedPreferences something2= getActivity().getSharedPreferences("something2",Context.MODE_PRIVATE);
		SharedPreferences.Editor editor2= something2.edit();
		editor2.putInt("counter_distracted", counter_distracted);
		editor2.putInt("counter_sleepy", counter_sleepy);
		editor2.putInt("counter_zoned", counter_zoned);
		editor2.commit();
	}

	/*
	 * This method runs all of the face detection methods on every camera frame
	 * The methods run detect if you are distracted, sleepy, zoning out, or too close to the screen
	 */
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		//transfers data from Calibration to the FragFD Class
		SharedPreferences something= getActivity().getSharedPreferences("something",Context.MODE_PRIVATE);
		int avgLength=something.getInt("average_length",0);
		float ReyeRcorner=something.getFloat("rEyeRCorner",0);
		float LeyeRcorner=something.getFloat("LEyeRCorner",0);
		float ReyeLcorner=something.getFloat("rEyeLCorner",0);
		float LeyeLcorner =something.getFloat("lEyeLCorner",0);

		if (closeDialog.test==true){ 
			//fdIsOn= true; 

			Runnable delayFD= new Runnable(){ 
				public void run() { 
					FragFD.fdIsOn= true; 
				} 
			}; 
			delayAgain.postDelayed(delayFD, 1000); 
			System.out.println("it's delayed!"); 
			closeDialog.test= false; 
		} 

		if (fdIsOn==true){
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();

			if (mAbsoluteFaceSize == 0) {
				int height = mGray.rows();
				if (Math.round(height * mRelativeFaceSize) > 0) {
					mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
				}
				mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
			}

			MatOfRect faces = new MatOfRect();

			if (mDetectorType == JAVA_DETECTOR) {
				if (mJavaDetector != null)
					mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
							new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
			}
			else if (mDetectorType == NATIVE_DETECTOR) {
				if (mNativeDetector != null)
					mNativeDetector.detect(mGray, faces);
			}
			else {
				Log.e(TAG, "Detection method is not selected!");
			}

			Rect[] facesArray = faces.toArray();


			//DISTRACTED METHOD
			distracted(facesArray);

			//OTHER METHODS
			if(facesArray.length>0){
				int i = facesArray.length - 1;//only takes rectangle of greatest size..only compatible with one person in this version
				Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
				int rectangleLength = facesArray[i].width;


				//EYESHIFT METHOD
				//create variables needed for eye area rectangles
				int eyearearightx=(int)(facesArray[i].x +facesArray[i].width/6.5);
				int eyeareay=(int)(facesArray[i].y + (facesArray[i].height/3.1));
				int eyedistance=(int)((facesArray[i].width - 2*facesArray[i].width/7)/1.7);
				int eyeareawidth = (int)((facesArray[i].width - 2*facesArray[i].width/7)/2.5);
				int eyeareaheight=(int)( facesArray[i].height/5.4);
				int eyearealeftx=eyearearightx + eyedistance;

				//creates eye area rectangles used for shifty eye detection
				eyearea_right = new Rect(eyearearightx,eyeareay,eyeareawidth,eyeareaheight);
				eyearea_left = new Rect(eyearealeftx,eyeareay, eyeareawidth, eyeareaheight);

				if(ReyeRcorner!=0 &&ReyeLcorner!=0 && LeyeRcorner!=0 &&LeyeLcorner!=0){
					//coordinate positions are all found with respect to the entire screen to compare with pupils that are also found for entire screen
					ReyeRcornerFinal = (int)(ReyeRcorner*facesArray[i].width + facesArray[i].x);
					LeyeRcornerFinal = (int)(LeyeRcorner*facesArray[i].width + facesArray[i].x);
					ReyeLcornerFinal = (int)(ReyeLcorner*facesArray[i].width + facesArray[i].x);
					LeyeLcornerFinal = (int)(LeyeLcorner*facesArray[i].width + facesArray[i].x);

					pupil_right = get_pupil(mEyeDetector, eyearea_right, 24);
					pupil_left = get_pupil(mEyeDetector, eyearea_left, 24);

					compareEyeCorners();
				}


				//BLINKING/SLEEPING METHODS
				//create rectangle used for blinking/sleeping detection
				eyearea_left2 = new Rect((int) (facesArray[i].x +facesArray[i].width/5 +(facesArray[i].width - 2*facesArray[i].width/8.5)/2),
						(int)(facesArray[i].y + (facesArray[i].height/3)), (int)(facesArray[i].width - 2*facesArray[i].width/4)/2,
						(int)( facesArray[i].height/8));
				leftEyeROI = mGray.submat(eyearea_left2);;

				detectBlink(leftEyeROI);


				//TOO CLOSE METHOD
				tooClose = false;
				System.out.println("avgLength: " + avgLength);
				System.out.println("avgLenth*zoom " + avgLength*zoomFactor);
				System.out.println("rectangleLength "+ rectangleLength);
				//tests if face is bigger than the default by the zoom factor)
				if (rectangleLength > (avgLength*zoomFactor)){
					tooClose = true;
				}
				else {
					tooClose= false;
				}
				if (tooClose==true) {
					fdIsOn=false;
					showClose();
				}
			}
		}

		//performs actions according to the emotion detected
		if  (emotion==DISTRACTED|| emotion == DISTRACTED_EYE_SHIFT){
			VidFrag.vidPause();
			showDistracted();
			fdIsOn=false;
		}

		if  (emotion==ZONEOUT){
			VidFrag.vidPause();
			showZoned();
			fdIsOn=false;
		}	

		if (emotion== SLEEPY){
			VidFrag.vidPause();
			MediaPlayer mp= MediaPlayer.create(getActivity().getApplicationContext(), R.raw.ding);
			showSleepy();
			mp.start();
			fdIsOn= false;
		}

		return mRgba;
	}


	/*
	 * Distracted helper method: 
	 * keeps track of the previous positions of the pupil in the eyeLocArray1 list
	 * If the position of the pupil is relatively constant, and then changes position suddenly, then the user is deemed to be distracted
	 * 
	 * faceCounter: keeps track of the number of times there is no face on the screen
	 */
	public void distracted(Rect[] facesArray){

		if(distracted_counter==5){
			distracted_counter=0;
		}

		boolean constantFrame = true;
		int NotConstantFrame = 0;

		if (facesArray.length ==0 || noEyes){//if there is no face in the screen with eyes or a face on screen with no eyes
			faceCounter+=1;
		}
		else{
			faceCounter=0;
		}

		//been unfocused for a period of time (no face for a long time)
		if (faceCounter == 15){

			//look through eyeLocArray to check if you were in a constant position before this by comparing one value to all the rest in the list
			for(int i = 0; i < eyeLocArray1.length; i++ ){
				int diff = Math.abs(eyeLocArray1[i]-eyeLocArray1[distracted_counter]);
				if(diff == 0 && eyeLocArray1[i]==0){//means that you were never focused on the screen (haven't had time to adjust yet/set up)
					constantFrame=false;
					break;
				}
				else if (diff > 40){//allots for one instance of noise to still be considered constant
					NotConstantFrame+=1;
				}
			}

			//distraction=constant frame most of the time
			if (constantFrame && NotConstantFrame<2 ){
				emotion = DISTRACTED;
				faceCounter=0;//reset to start again
			}
		}
	}


	/* EYESHIFT HELPER METHODS includes:
	 * 		-getting the pupil
	 * 		-comparing eye corners with pupil location
	 */

	private Point pupil_right;
	private Point pupil_left;

	//returns the pupil's position as a point
	private Point get_pupil(CascadeClassifier clasificator, Rect area, int size) {
		if (distracted_counter==eyeLocArray1.length){
			distracted_counter = 0;
		}

		Mat roi = mGray.submat(area);
		MatOfRect eyes = new MatOfRect();
		Point pupil = new Point();
		clasificator.detectMultiScale(roi,eyes,1.15,2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE,
				new Size(30,30),new Size());

		eyesArray = eyes.toArray();
		if(eyesArray.length==1){//if there is only one face that is focused on
			Rect e = eyesArray[0];
			e.x = area.x+e.x;
			e.y = area.y+e.y;
			Rect eyeRectangle = new Rect( (int) e.tl().x, (int)(e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
			roi = mGray.submat(eyeRectangle);

			//find the darkest point in the eye rectangle
			Core.MinMaxLocResult darkPoint = Core.minMaxLoc(roi); 
			pupil.x = darkPoint.minLoc.x + eyeRectangle.x;
			pupil.y = darkPoint.minLoc.y + eyeRectangle.y;

			if (area == eyearea_right){
				eyeLocArray1[distracted_counter]= (int)pupil.x;
			}   
			else if(area == eyearea_left){
				eyeLocArray2[distracted_counter] = (int)pupil.x;
			}
		}
		distracted_counter++;
		return pupil;
	}

	/*compares pupil position with eye corner position
	 * eyeCornerCounter: keeps track of how many times the pupil's position is near an eye corner
	 */
	private void compareEyeCorners(){
		int rEyeRCornerDiff = (int)Math.abs(pupil_right.x-ReyeRcornerFinal);
		int rEyeLCornerDiff= (int)Math.abs(pupil_right.x -ReyeLcornerFinal);
		int lEyeRCornerDiff= (int)Math.abs(pupil_left.x-LeyeRcornerFinal);
		double eyeLengthLeft = Math.abs(LeyeLcornerFinal-LeyeRcornerFinal);
		double eyeLengthRight=Math.abs(ReyeLcornerFinal-ReyeRcornerFinal);

		if(pupil_right.x !=0 &&pupil_left.x !=0){//if both pupils are detected
			if(lEyeRCornerDiff <(.3)*eyeLengthLeft && rEyeRCornerDiff <(.3 )*eyeLengthRight || rEyeLCornerDiff <(.3 )*eyeLengthRight){
				eyeCornerCounter++;
			}
			else{
				eyeCornerCounter=0;
			}
		}
		else if(pupil_right.x!=0){//if only one pupil is detected
			if(rEyeRCornerDiff <(.3)*eyeLengthRight || rEyeLCornerDiff <(.3)*eyeLengthRight){
				eyeCornerCounter++;
			}
			else{
				eyeCornerCounter=0;
			}
		}
		else if(pupil_left.x!=0){//if only one pupil is detected
			if(lEyeRCornerDiff <(.3)*eyeLengthLeft ){
				eyeCornerCounter++;
			}
			else{
				eyeCornerCounter=0;
			}
		}
		else{
		}

		if(eyeCornerCounter>3){
			emotion=DISTRACTED_EYE_SHIFT;
		}
	}



	/* SLEEPY/ZONING OUT HELPER METHODS
	 * 
	 * smallCounter: counts instances of sleepiness
	 * bigCounter: counts instances of zoning out
	 */
	public static int smallCounter, bigCounter; 

	public void detectBlink(Mat region) { 
		SharedPreferences something= getActivity().getSharedPreferences("something",Context.MODE_PRIVATE); 
		float fAvg=something.getFloat("averagefraction",0); 

		Mat roi = region; 
		Mat destMat = new Mat(); 
		Imgproc.adaptiveThreshold(roi, destMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 1); 
		destMat.copyTo(roi); 

		//counts number of white and black pixels present in the black and white filtered camera image
		float whitePixels = Core.countNonZero(roi); 
		float totalPixels = roi.width()*roi.height(); 
		float blackPixels = totalPixels-whitePixels; 

		float f = (blackPixels/whitePixels); 

		if ((f-fAvg) >-0.08 && previous==false) { 
			bigCounter++; 
			previous=true; 
			if (bigCounter > 75){ 
				bigCounter=0; 
				emotion=ZONEOUT; 
			} 
			else if (smallCounter >7){ 
				smallCounter=0; 
				emotion=SLEEPY; 
			} 
		} 
		else if ((f-fAvg)>-0.08 && previous==true) { 
			bigCounter++; 
			smallCounter=0; 

			if (bigCounter > 75){ 
				bigCounter=0; 
				emotion=ZONEOUT; 
			} 
			else if (smallCounter >7){ 
				smallCounter=0; 
				emotion=SLEEPY; 
			} 
		} 
		else if ((f-fAvg<-.08) && previous==true) { 
			smallCounter++; 
			previous = false; 
			if (bigCounter > 75){ 
				bigCounter=0; 
				emotion=ZONEOUT; 
			} 
			else if (smallCounter >7){ 
				smallCounter=0; 
				emotion=SLEEPY; 
			} 
		} 
		else if ((f-fAvg<-.08) && previous==false) {
			smallCounter++; 
			bigCounter = 0; 
			if (smallCounter >7){ 
				smallCounter=0; 
				emotion=SLEEPY; 
			} 
			else if (bigCounter > 75){ 
				bigCounter=0; 
				emotion=ZONEOUT; 
			} 
		} 
	} 

	/*
	 * ACTIONS AFTER EMOTIONS DETERMINED
	 * 		the following methods open up dialog boxes that pop up on the screen following the detection of a specific emotion
	 * and then sets the emotion back to DEFAULT
	 */
	public void showDistracted(){
		FragmentManager fm= getActivity().getSupportFragmentManager();
		distractedDialog distractedDialog= new distractedDialog();
		if(!VidFrag.quizTime) {
			distractedDialog.show(fm, "distractedTag");
			time = VidFrag.vidPlayer.getCurrentPosition();
			emotion = DEFAULT;
			distractedDialog.setEmotion(org.opencv.samples.facedetect.emotionDialog.DEFAULTdialog);
			resetCounters();
		}
	}
	public void showSleepy(){
		FragmentManager fm= getActivity().getSupportFragmentManager();
		sleepDialog sleepDialog= new sleepDialog();
		if(!VidFrag.quizTime){
			sleepDialog.show(fm, "sleepTag");
			time = VidFrag.vidPlayer.getCurrentPosition();
			emotion = DEFAULT;
			sleepDialog.setEmotion(org.opencv.samples.facedetect.emotionDialog.DEFAULTdialog);
			resetCounters();
		}
	}
	public void showZoned(){
		FragmentManager fm= getActivity().getSupportFragmentManager();
		zoneDialog zoneDialog= new zoneDialog();
		if(!VidFrag.quizTime){
			zoneDialog.show(fm, "zonedTag");
			time = VidFrag.vidPlayer.getCurrentPosition();
			zoneDialog.setEmotion(org.opencv.samples.facedetect.zoneDialog.DEFAULTdialog);
			emotion = DEFAULT;
			resetCounters();
		}
	}

	public void showClose(){
		FragmentManager fm= getActivity().getSupportFragmentManager();
		closeDialog closeDialog= new closeDialog();
		if(!VidFrag.quizTime){
			closeDialog.show(fm, "toocloseness");
			resetCounters();
		}
	}


	/*
	 * If the emotion detected was actually correct (aka the user clicked "Yes, I was distracted" for example),
	 * this method adds the emotion and the time at which it occurred to a list that will be displayed at the end of the video 
	 */
	public static void checkEmotion(int emotdialog){
		int emotDialog = emotdialog;
		if(emotDialog!=org.opencv.samples.facedetect.emotionDialog.DEFAULTdialog){
			int min = (int)(time/1000.0)/60;
			int sec = (int)(time/1000.0 - min*60.0);
			if(emotDialog==org.opencv.samples.facedetect.emotionDialog.DISTRACTEDdialog ||emotDialog==org.opencv.samples.facedetect.emotionDialog.DISTRACTED_EYE_SHIFTdialog){
				emotionList.add(min + "min " + sec + "sec" + " Distracted");
				emotionListNumber.add(DISTRACTED);
			}
			else if(emotDialog==org.opencv.samples.facedetect.emotionDialog.SLEEPYdialog) {
				emotionList.add(min + "min " + sec + "sec" + " Sleepy");
				emotionListNumber.add(SLEEPY);
			}
			else{//emotion==ZONEDOUT
				emotionList.add(min + "min " + sec + "sec" +" Zoning out");
				emotionListNumber.add(ZONEOUT);
			}
		}
	}

	//helper method for checkEmotion method that resets counters if an emotion has been detected
	public static void resetCounters(){
		Log.i("emotionlist", "resetCounters");
		faceCounter=0;
		eyeCornerCounter=0;
		bigCounter=0;
		smallCounter=0;
	}
}  