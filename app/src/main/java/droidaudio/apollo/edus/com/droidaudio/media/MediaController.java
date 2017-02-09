package droidaudio.apollo.edus.com.droidaudio.media;

import android.content.Context;

import droidaudio.apollo.edus.com.droidaudio.media.audio.MediaRecorderWrapper;

/**
 * 录音和播放录音的框架
 * 可提供不同方式的录音和不同格式的播放
 * Created by PandaPan on 2017/2/6.
 */

public class MediaController{

    private Context mContext;
    private IRecorderListener mRecorderListener;

    private volatile static MediaController sInstance;

    private IRecorder mRecorder;
    private IPlayer mPlayer;

    private MediaController(){

    }

    public static MediaController getInstance(){
        if(sInstance == null){
            synchronized (MediaController.class){
                if(sInstance == null){
                    sInstance = new MediaController();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context){
        if(context != null){
            mContext = context.getApplicationContext();
        }
    }

    public synchronized void startRecording(final IRecorderListener listener){
        mRecorderListener = listener;
        releasePlayer();
        if(mRecorder != null){
            if(!mRecorder.isRecording()){
                mRecorder.startRecording();
            }
        }else{
            mRecorder = new MediaRecorderWrapper(mContext, new IRecorderListener() {
                @Override
                public void onStart(String filePath) {
                    if(mRecorderListener != null){
                        mRecorderListener.onStart(filePath);
                    }
                }

                @Override
                public void onStop(String filePath, long duration) {
                    if(mRecorderListener != null){
                        mRecorderListener.onStop(filePath, duration);
                        mRecorderListener = null;
                    }
                }

                @Override
                public void onError(int errorReason, int what, int extra) {
                    if(mRecorderListener != null){
                        mRecorderListener.onError(errorReason, what, extra);
                        mRecorderListener = null;
                    }
                }
            });
        }
    }

    public synchronized void stopRecording(){
        releaseRecorder();
    }

    private void releasePlayer(){
        if(mPlayer != null){
            mPlayer.stop();
            mPlayer = null;
        }
    }

    private void releaseRecorder(){
        if(mRecorder != null){
            if(mRecorder.isRecording()){
                mRecorder.stopRecording();
            }
            mRecorder = null;
        }
    }

}
