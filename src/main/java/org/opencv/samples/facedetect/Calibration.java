package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
/*
 * This class runs during calibration at the beginning of the app. It takes in the following pieces of data that are needed 
 * to perform face detection in the FragFD class:
 * 		-eye corner fractions 
 * 		-average length of the face rectangle
 */
public class Calibration extends Activity implements CvCameraViewListener2 {

	//defining variables
	private static final String    TAG                 = "OCVSample::Activity";
	private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
	public static final int        JAVA_DETECTOR       = 0;
	public static final int        NATIVE_DETECTOR     = 1;

	private Mat                    mRgba;
	private Mat                    mGray;
	private File                   mCascadeFile,mCascadeFileEye;
	private CascadeClassifier      mJavaDetector,mEyeDetector;
	private DetectionBasedTracker  mNativeDetector,mEyeNativeDetector;

	private int frames,rectLength,totalLength;
	public int avgLength=5;

	private int                    mDetectorType       = JAVA_DETECTOR;
	private String[]               mDetectorName;

	private float                  mRelativeFaceSize   = 0.2f;
	private int                    mAbsoluteFaceSize   = 0;
	private Rect[]                 eyesArray;
	private Rect[] facesArray;

	//all represent fractions of the face array that the corners are at
	private float totalrightEyeRightCorner=0;
	private float totalrightEyeLeftCorner=0;
	private float totalleftEyeRightCorner=0;
	private float totalleftEyeLeftCorner=0;
	private float rightEyeRightCorner;
	private float rightEyeLeftCorner;
	private float leftEyeRightCorner;
	private float leftEyeLeftCorner;
	private int RERCCount=0;
	private int RELCCount=0;
	private int LERCCount=0;
	private int LELCCount=0;
	private int divideBy=0;
	private int sleepCounter = 0;
	private float fTotal = 0;
	public float fAvg = 0;
	private Rect eyearea_right;
	private Rect eyearea_left;
	private Rect eyearea_left2; 

	private JavaCameraView   mOpenCvCameraView;
	Timer timer;
	Handler attempt= new Handler();
	Handler attempt2= new Handler();
	Intent listIntent= new Intent();
	Intent varIntent= new Intent();
	private ProgressBar mProgress; 
	private int mProgressStatus=0; 
	private static int totalTime = 10500;
	private int n;

	private Typeface font;
	private TextView caliText;

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
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
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					InputStream isEye = getResources().openRawResource(R.raw.haarcascade_eye);
					File eyecascadeDir = getDir("cascadeEye", Context.MODE_PRIVATE);
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

	public Calibration() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.calibration);
		//		font = Typeface.createFromAsset(this.getAssets(),"ArchitectsDaughter.ttf");
		//        caliText = (TextView) findViewById(R.id.textView1);
		//        caliText.setTypeface(font);
		mOpenCvCameraView = (JavaCameraView) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mProgress = (ProgressBar)findViewById(R.id.progress_bar); 

	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

		//creates a progress bar that counts down during calibration
		CountDownTimer t = new CountDownTimer(totalTime, 100){ 
			public void onFinish(){ 
				mProgressStatus = 100; 
				mProgress.setProgress(mProgressStatus); 
			}
			@Override public void onTick(long millisUntilFinished) { 
				float fraction = (totalTime -millisUntilFinished)/(float)totalTime; 
				mProgressStatus = (int)(fraction *100); 
				mProgress.setProgress(mProgressStatus);     
			} 
		}; 
		t.start(); 
		attempt.postDelayed(toList,10500);//goes to next class (PlaylistActivity class) after 10.5 seconds
	}

	private Runnable toList= new Runnable(){
		public void run(){
			listIntent.setClass(getApplicationContext(), playlistActivity.class); //changed
			listIntent.setAction(Intent.ACTION_CALL);
			startActivity(listIntent);
		};
	};

	public void onDestroy() {
		super.onDestroy();

		attempt.removeCallbacks(toList);
		mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		SharedPreferences something= this.getSharedPreferences("something",Context.MODE_PRIVATE);
		SharedPreferences.Editor editor= something.edit();
		editor.clear();
		editor.commit();
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();

		SharedPreferences something= this.getSharedPreferences("something",MODE_PRIVATE);
		SharedPreferences.Editor editor= something.edit();
		editor.putInt("average_length", avgLength);
		editor.putFloat("rEyeLCorner", rightEyeLeftCorner);
		editor.putFloat("LEyeRCorner", leftEyeRightCorner);
		editor.putFloat("rEyeRCorner", rightEyeRightCorner);
		editor.putFloat("lEyeLCorner", leftEyeLeftCorner);
		editor.putFloat("averagefraction", fAvg);
		editor.commit();
	}


	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
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

		facesArray = faces.toArray();
		if (n < 71) {
			if (frames < 70) {
				//gets the biggest rectangle being displayed on the screen
				if(facesArray.length>0){
					Core.rectangle(mRgba, facesArray[facesArray.length-1].tl(), facesArray[facesArray.length-1].br(), FACE_RECT_COLOR, 3);
					rectLength = facesArray[facesArray.length - 1].width;
					if (rectLength >0) {
						totalLength+=rectLength;
						divideBy++;
					}
				}
				frames++;
			}
			if(divideBy!=0){
				avgLength = totalLength/divideBy;
			}
			n++;

			if(sleepCounter!=0){
				//fAvg: ratio of black/white
				fAvg = fTotal/sleepCounter;
			}

			//eye corner counters to get the average eye corner positions
			if(RERCCount!=0){
				rightEyeRightCorner = (totalrightEyeRightCorner/RERCCount);
			}
			if(RELCCount!=0){
				rightEyeLeftCorner= (totalrightEyeLeftCorner/RELCCount);
			}
			if(LERCCount!=0){
				leftEyeRightCorner=(totalleftEyeRightCorner/LERCCount);
			}
			if(LELCCount!=0){
				leftEyeLeftCorner=(totalleftEyeLeftCorner/LELCCount);
			}
		}

		if(facesArray.length ==1){
			calibrateEyeCorners();
		}
		return mRgba;
	}

	private Point pupil_right;
	private Point pupil_left;

	//finds all points found with respect to wherever the face rectangle is as a fraction of the face's width
	private void calibrateEyeCorners(){
		//variable set up for the eye area rectangle
		int eyearearightx=(int)(facesArray[0].x +facesArray[0].width/6.5);
		int eyeareay=(int)(facesArray[0].y + (facesArray[0].height/3.1));
		int eyedistance=(int)((facesArray[0].width - 2*facesArray[0].width/7)/1.7);
		int eyeareawidth = (int)((facesArray[0].width - 2*facesArray[0].width/7)/2.5);
		int eyeareaheight=(int)( facesArray[0].height/5.4);
		int eyearealeftx=eyearearightx + eyedistance;

		//eye area rectangles used to find eye corners
		eyearea_right = new Rect(eyearearightx,eyeareay,eyeareawidth,eyeareaheight);
		eyearea_left = new Rect(eyearealeftx,eyeareay, eyeareawidth, eyeareaheight);
		Core.rectangle(mRgba, new Point(eyearearightx, eyeareay), new Point(eyearearightx + eyeareawidth, eyeareay + eyeareaheight),new Scalar(0,0,255));
		Core.rectangle(mRgba, new Point(eyearealeftx, eyeareay),new Point(eyearealeftx + eyeareawidth, eyeareay + eyeareaheight),new Scalar(0,255,0));
		Mat eyeareaROIleft = mGray.submat(eyearea_left);
		Mat eyeareaROIright = mGray.submat(eyearea_right);
		pupil_right = get_pupil(mEyeDetector, eyearea_right, 24);//finds pupil with respect to eyearea_right's location
		pupil_left = get_pupil(mEyeDetector, eyearea_left, 24);
		Mat eyeROIleft = new Mat();
		Mat eyeROIright = new Mat();

		eyeareaROIleft.convertTo(eyeROIleft,-1,2);
		eyeareaROIright.convertTo(eyeROIright,-1,2);

		Imgproc.adaptiveThreshold(eyeROIleft, eyeROIleft, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 1);
		Imgproc.adaptiveThreshold(eyeROIright, eyeROIright, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 1);

		//eye area rectangle used to find ratio of black and white pixels of a typical opened eye
		eyearea_left2 = new Rect((int) (facesArray[0].x +facesArray[0].width/5 +(facesArray[0].width - 2*facesArray[0].width/8.5)/2),
				(int)(facesArray[0].y + (facesArray[0].height/3)), (int)(facesArray[0].width - 2*facesArray[0].width/4)/2,
				(int)( facesArray[0].height/8));
		Mat leftEyeROI = mGray.submat(eyearea_left2);
		Mat leftEyeROIdest = new Mat();
		Imgproc.adaptiveThreshold(leftEyeROI, leftEyeROIdest, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 1);

		float whitePixels = Core.countNonZero(leftEyeROIdest);
		float totalPixels = leftEyeROIdest.width()*leftEyeROIdest.height();
		float blackPixels = totalPixels-whitePixels;
		float f = (blackPixels/whitePixels);
		if (f!=0.0) {
			sleepCounter++;
			fTotal+=f;
		}

		if (pupil_right.x!=0 && pupil_left.x!=0){
			//gets corners whose positions are given in respect to the whole screen
			double corner1 = get_eye_corner_right(eyeROIright, eyeareaROIright,eyearea_right,true);
			double corner2 = get_eye_corner_left(eyeROIright, eyeareaROIright, eyearea_right,true);
			double corner3 = get_eye_corner_right(eyeROIleft, eyeareaROIleft, eyearea_left,false);
			double corner4= get_eye_corner_left(eyeROIleft, eyeareaROIleft, eyearea_left,false);

			//gets position with respect to faceArray rectangle and the divides by rectangle length to get fraction of rectangle
			if(corner1!=-1){
				corner1 = (eyearearightx - facesArray[0].x +corner1)/facesArray[0].width;
				totalrightEyeRightCorner+=corner1;
				RERCCount++;
			}
			if(corner2!=-1){
				corner2 = (eyearearightx - facesArray[0].x + corner2)/facesArray[0].width;
				totalrightEyeLeftCorner+=corner2;
				RELCCount++;
			}
			if(corner3!=-1){
				corner3 = (eyearealeftx - facesArray[0].x + corner3)/facesArray[0].width;
				totalleftEyeRightCorner+=corner3;
				LERCCount++;
			}
			if(corner4!=-1){
				corner4 = (eyearealeftx - facesArray[0].x + corner4)/facesArray[0].width;
				totalleftEyeLeftCorner+=corner4;
				LELCCount++;
			}
		}
	}


	//returns pupil location as a point with respect to the whole screen
	private Point get_pupil(CascadeClassifier clasificator, Rect area, int size) {
		Mat roi = mGray.submat(area);
		MatOfRect eyes = new MatOfRect();
		Point pupil = new Point();
		clasificator.detectMultiScale(roi,eyes,1.15,2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE,
				new Size(30,30),new Size());

		eyesArray = eyes.toArray();
		if(eyesArray.length==1){
			Rect e = eyesArray[0];
			e.x = area.x+e.x;
			e.y = area.y+e.y;
			Rect eyeRectangle = new Rect( (int) e.tl().x, (int)(e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
			roi = mGray.submat(eyeRectangle);
			Mat mroi = mRgba.submat(eyeRectangle);
			Core.MinMaxLocResult darkPoint = Core.minMaxLoc(roi); //finds darkest point to get pupil

			//draw point at pupil
			Core.circle(mroi, darkPoint.minLoc, 2, new Scalar(255,255,255,255),2);
			pupil.x = darkPoint.minLoc.x + eyeRectangle.x;
			pupil.y = darkPoint.minLoc.y + eyeRectangle.y;
		}
		return pupil;
	}

	/*helper method that actually gets eye corner with respect the WHOLE SCREEN
	 * starts from the far edges of the eye area rectangle and stops at the first point that is black and whose
	 * neighboring pixels are also black
	 */
	private int get_eye_corner_left(Mat eyeROIbinary, Mat eyeROIorig, Rect eyearea,boolean isRight){
		double color;
		if(isRight){
			for(int col = (int)(eyearea.width-3); col > 0; col-=2){
				for(int row = (int)(eyearea.height/5); row < 2*eyearea.height/3; row+=2){
					color = eyeROIbinary.get(row, col)[0];//gets color at that point in binary mat
					if(color==0){//black
						if(evaluateNeighbors(row, col, eyeROIbinary)==true){//if all neighboring pixels are black
							return col;
						}
					}
				}
			}
		}
		else{//isLeft
			for(int col = (int)(5*eyearea.width/6); col > eyearea.width/2; col-=2){
				for(int row = (int)(eyearea.height/5); row < 2*eyearea.height/3; row+=2){
					color = eyeROIbinary.get(row, col)[0];
					if(color ==0){//black
						if(evaluateNeighbors(row, col, eyeROIbinary)==true){//all black
							return col;
						}
					}
				}
			}
		}
		return -1;
	}

	//gets right corner coordinates in respect to the whole screen
	private int get_eye_corner_right(Mat eyeROIbinary, Mat eyeROIorig, Rect eyearea, boolean isRight){
		double color;
		if(isRight){
			for(int col = (int)(eyearea.width/3); col < eyearea.width/2; col+=2){
				for(int row = (int)(eyearea.height/5); row < 2*eyearea.height/3; row+=2){
					color = eyeROIbinary.get(row,col)[0];

					if(color ==0){//black
						if(evaluateNeighbors(row, col, eyeROIbinary)==true){//all black
							return col;
						}
					}
				}
			}
		}
		else{//left eye
			for(int col = 2; col < eyearea.width/2; col+=2){
				for(int row = (int)(eyearea.height/5); row < 2*eyearea.height/3; row+=2){
					color = eyeROIbinary.get(row, col)[0];
					if(color ==0){//black
						if(evaluateNeighbors(row, col, eyeROIbinary)==true){//all black
							return col;
						}
					}
				}
			}
		}
		return -1;
	}


	//only returns true (aka point is eye corner) if all neighbors around it are black as well
	private boolean evaluateNeighbors(int row, int col, Mat eyeROIbinary){
		double color;
		for(int x = -1; x <2; x++){
			for(int y=-1; y <2; y++){
				color=eyeROIbinary.get(row + y, col+x)[0];
				if(color!=0){
					return false;
				}
			}
		}
		return true;
	}
}
