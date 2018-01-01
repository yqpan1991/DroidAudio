package droidaudio.apollo.edus.com.droidaudio.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Description.
 *
 * @author panda
 */

public class MainLooper {
    private static MainLooper sInstance = new MainLooper();
    private Handler mMainHandler;

    public static MainLooper instance(){
        return sInstance;
    }

    private MainLooper(){
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void post(Runnable runnable){
        if(runnable != null){
            mMainHandler.post(runnable);
        }
    }
}
