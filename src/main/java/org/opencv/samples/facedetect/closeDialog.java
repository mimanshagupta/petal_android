package org.opencv.samples.facedetect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
/*
 * This class creates a dialog box that pops up when the user is too close to the screen
 * restarts face detection after user clicks a button
 */
public class closeDialog extends DialogFragment{
	public static boolean test= false; 
	public closeDialog (){	
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		super.onCreateDialog(savedInstanceState);
		test= false; 
		AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(getActivity());
		View view= getActivity().getLayoutInflater().inflate(R.layout.closedialog, null);
		alertDialogBuilder.setView(view);

		Bitmap tooclose= BitmapFactory.decodeResource(getResources(), R.drawable.petalicontooclose);

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

		TextView tv = (TextView) view.findViewById(R.id.closeQuestion); 
		tv.setTypeface(font); 
		alertDialogBuilder.setTitle(tv.getId()); 

		alertDialogBuilder.setPositiveButton("Okay, I'm ready!", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int idk){
				((AlertDialog)dialog).getButton(idk);
				dialog.cancel();
				test = true;
			}
		}
				);

		final AlertDialog alert= alertDialogBuilder.create(); 
		alert.setOnShowListener(new DialogInterface.OnShowListener(){ 
			@Override
			public void onShow(DialogInterface dialog){ 
				Typeface font=Typeface.createFromAsset(getActivity().getAssets(),"ArchitectsDaughter.ttf"); 

				Button positive= alert.getButton(Dialog.BUTTON_POSITIVE); 
				positive.setTextSize(18); 
				positive.setTypeface(font); 
				positive.setBackgroundColor(Color.argb(0, 0, 0, 0)); 
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

	@Override
	public void onDismiss(DialogInterface dialog){ 
		super.onDismiss(dialog); 
	} 
}