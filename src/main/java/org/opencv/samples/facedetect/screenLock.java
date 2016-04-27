package org.opencv.samples.facedetect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * stops activity if the screen is locked/turned off
 * restarts activity if screen is unlocked/on
 */
public class screenLock extends BroadcastReceiver {

	public static boolean wasScreenOn= true;
	
	@Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            VidFrag.vidPause();
            wasScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            wasScreenOn = true;
        }
    }
}
