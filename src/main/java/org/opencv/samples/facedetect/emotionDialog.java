package org.opencv.samples.facedetect;

import android.support.v4.app.DialogFragment;

/*
 * Parent class of all the other dialog boxes so that they all have access to the same emotions variables
 */
public class emotionDialog extends DialogFragment{
	protected static int DISTRACTEDdialog = 0;
	protected static int DISTRACTED_EYE_SHIFTdialog = 1;
	protected static int SLEEPYdialog = 2;
	protected static int ZONEOUTdialog = 3;
	protected static int DEFAULTdialog = -1;
	protected int emotion =DEFAULTdialog;

	public emotionDialog(){
	}

	public int getEmotion(){
		return emotion;
	}
	
	public void setEmotion(int emot){
		emotion = emot;
	}
}
