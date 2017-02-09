package droidaudio.apollo.edus.com.droidaudio.media.audio;

import android.os.Handler;
import android.os.Looper;

import droidaudio.apollo.edus.com.droidaudio.media.IPlayer;
import droidaudio.apollo.edus.com.droidaudio.media.IPlayerListener;

/**
 * Created by PandaPan on 2017/2/7.
 */

public abstract class BasePlayer implements IPlayer {

    private IPlayerListener mPlayerListener;
    private Handler mUiHandler;

    public BasePlayer(){
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public void setPlayerListener(IPlayerListener playerListener){
        mPlayerListener = playerListener;
    }

    protected void notifyOnPrepared(final String filePath){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onPreparing(filePath);
                }
            }
        });
    }

    protected void notifyOnPreparing(final String filePath){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onPrepared(filePath);
                }
            }
        });
    }

    protected void notifyOnPlay(final String filePath){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onPlay(filePath);
                }
            }
        });
    }

    protected void notifyOnPause(final String filePath){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onPause(filePath);
                }
            }
        });
    }

    protected void notifyOnError(final String filePath, final int what, final int extra){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onError(filePath, what, extra);
                }
            }
        });
    }

    protected void notifyOnStopped(final String filePath){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onStopped(filePath);
                }
            }
        });
    }

    protected void notifyOnComplete(final String filePath){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onComplete(filePath);
                }
            }
        });
    }

    protected void notifyOnProgressChanged(final String filePath, final int targetPosition, final int duration){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPlayerListener != null){
                    mPlayerListener.onProgressChanged(filePath, targetPosition, duration);
                }
            }
        });
    }





}
