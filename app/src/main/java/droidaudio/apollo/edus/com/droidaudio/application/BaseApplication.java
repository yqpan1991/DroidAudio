package droidaudio.apollo.edus.com.droidaudio.application;

import android.app.Application;

import droidaudio.apollo.edus.com.droidaudio.multimedia.MediaManager;

/**
 * Created by panda on 2018/1/1.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        MediaManager.getInstance().init(getApplicationContext());
    }


}
