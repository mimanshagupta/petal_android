package org.opencv.samples.facedetect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
 * creates dialog after user is deemed to be zoning out
 */
public class zoneDialog  extends emotionDialog{
	protected static int DEFAULTdialog = -1;
	protected static int ZONEOUTdialog = 3;
	private int emotion= DEFAULTdialog;
	public zoneDialog (){
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		super.onCreateDialog(savedInstanceState);
		AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(getActivity());

		View view= getActivity().getLayoutInflater().inflate(R.layout.zoneddialog, null);
		alertDialogBuilder.setView(view);

		Bitmap tooclose= BitmapFactory.decodeResource(getResources(), R.drawable.petaliconzoningout);
		int width= tooclose.getWidth();
		int height= tooclose.getHeight();
		int newWidth= 100;
		int newHeight= 100;

		float scaleWidth= ((float) newWidth)/width;
		float scaleHeight= ((float) newHeight)/height;

		Matrix mat= new Matrix();
		mat.postScale(scaleWidth, scaleHeight);

		Bitmap resizedBitmap= Bitmap.createBitmap(tooclose,0,0, width, height, mat, true);

		Drawable icon= new BitmapDrawable(getResources(),resizedBitmap);
		alertDialogBuilder.setIcon(icon);
		Typeface font=Typeface.createFromAsset(getActivity().getAssets(),"ArchitectsDaughter.ttf"); 
		TextView tv = (TextView) view.findViewById(R.id.zonedQuestion); 
		tv.setTypeface(font); 
		alertDialogBuilder.setTitle(tv.getId());  

		alertDialogBuilder.setPositiveButton("Yes, please rewind a bit!", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int idk){
				((AlertDialog)dialog).getButton(idk);
				VidFrag.vidSeek();
				emotion=ZONEOUTdialog;
				dialog.cancel();
				FragFD.fdIsOn = true;
				FragFD.resetCounters();
				FragFD.checkEmotion(emotion);
			}
		}
				)
				.setNegativeButton("No, I'm paying attention!", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int idk){
						((AlertDialog)dialog).getButton(idk);
						dialog.cancel();
						FragFD.fdIsOn = true;
						emotion=DEFAULTdialog;
						VidFrag.vidResume();
						FragFD.resetCounters();
					}
				});
		final AlertDialog alert= alertDialogBuilder.create(); 
		alert.setOnShowListener(new DialogInterface.OnShowListener(){ 
			@Override
			public void onShow(DialogInterface dialog){ 
				Typeface font=Typeface.createFromAsset(getActivity().getAssets(),"ArchitectsDaughter.ttf"); 

				Button positive= alert.getButton(Dialog.BUTTON_POSITIVE); 
				positive.setTextSize(18); 
				positive.setTypeface(font); 
				positive.setBackgroundColor(Color.argb(0, 0, 0, 0)); 

				Button negative= alert.getButton(Dialog.BUTTON_NEGATIVE); 
				negative.setTextSize(18); 
				negative.setBackgroundColor(Color.argb(0, 0, 0, 0)); 
				negative.setTypeface(font); 

				alert.setTitle(""); 
			} 

		}); 
		return alert; 
	}
	@Override
	public void onStart(){ 
		super.onStart(); 
		this.getDialog().setCanceledOnTouchOutside(false); 
	} 
}
