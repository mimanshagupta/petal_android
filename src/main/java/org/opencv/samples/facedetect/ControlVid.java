package org.opencv.samples.facedetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
//import android.widget.MediaController.MediaPlayerControl;
import android.widget.VideoView;

/*
 * controls the video and sets a playpause listener to the video
 */
public class ControlVid extends VideoView implements SurfaceHolder{
	
    private PlayPauseListener mListener; //responds to play pause being pressed
    public ControlVid(Context context) {
        super(context);
    }
    public ControlVid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ControlVid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }
    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }
    
    @Override
    protected void onMeasure(int width, int height){
    	setMeasuredDimension(480,800);
    }
    

    interface PlayPauseListener {
        void onPlay();
        void onPause();
    }


	@Override
	public void addCallback(Callback arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Surface getSurface() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Rect getSurfaceFrame() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isCreating() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Canvas lockCanvas() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Canvas lockCanvas(Rect arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void removeCallback(Callback arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setFixedSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setFormat(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setSizeFromLayout() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void unlockCanvasAndPost(Canvas arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	@Deprecated
	public void setType(int arg0) {
		// TODO Auto-generated method stub
		
	}
    
}