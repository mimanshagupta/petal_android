package org.opencv.samples.facedetect;

import android.app.Application;
import android.content.res.Configuration;

import com.firebase.client.Firebase;
import com.parse.Parse;

/**
 * Created by Mimansha on 20/1/2016.
 */
public class PetalApplication extends Application {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        Parse.addParseNetworkInterceptor(new ParseLogInterceptor());
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}