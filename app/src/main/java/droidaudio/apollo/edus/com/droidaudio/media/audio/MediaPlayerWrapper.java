package droidaudio.apollo.edus.com.droidaudio.media.audio;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import droidaudio.apollo.edus.com.droidaudio.media.IPlayer;

/**
 * Created by PandaPan on 2017/2/6.
 * 添加handler的机制,需要向外回调音乐播放器的进度
 * 在不存在音乐播放的path时,调用resume,pause,seekTo会抛出异常的回调
 *
 */

public class MediaPlayerWrapper extends BasePlayer {
    private static final int MSG_WHAT_NOTIFY_PROGRESS = 1000;
    private static final int NOTIFY_PROGRESS_DELAY = 1000;
    private final String TAG = this.getClass().getSimpleName();
    private String mFilePath;
    private MediaPlayer mMediaPlayer;
    private int mState;
    private int mTargetPosition;



    public MediaPlayerWrapper(){
        super();
        init();
    }

    @Override
    public void start(String localPath) {
        //检查是否有当前播放的数据,如果存在,比对是否相同,如果相同,调用继续播放
        //如果不相同,释放上次的mediaPlayer,继续
        //检查localPath的合法性,如果不合法,返回错误,如果合法,初始化MediaPlayer,然后做seekTo的操作
        if(!TextUtils.isEmpty(mFilePath)){
            if(mFilePath.equals(localPath)){
                resume();
                return;
            }else{
                String filePath = mFilePath;
                init();
                notifyOnStopped(filePath);
            }
        }
        mFilePath = localPath;
        seekTo(0);
    }

    @Override
    public void pause() {
        if(mState == IPlayer.RUNNING){
            try{
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    notifyOnPause(mFilePath);
                }
            }catch (IllegalStateException ex){
                handleErrorEncounted(0 , 0);
            }
        }else if(mState == IPlayer.IDLE || mState == IPlayer.INITIALIZED){
            handleErrorEncounted(0, 0);
        }
    }

    @Override
    public void stop() {
        //检查状态和文件,如果不合法,释放资源,然后向外报错
        //如果合法,执行stop的操作,并且设置当前的播放状态
        if(mState == IPlayer.RUNNING || mState == IPlayer.PREPARED || mState == IPlayer.PREPARING || mState == IPlayer.INITIALIZED){
            String filePath = mFilePath;
            init();
            notifyOnStopped(filePath);
        }else{
            handleErrorEncounted(0 , 0);
        }
    }

    @Override
    public void resume() {
        //检查状态,是否合法,然后调用start方法即可
        if(mState == IPlayer.RUNNING){
            try{
                if(mMediaPlayer.isPlaying()){
                    //do nothing
                }else{
                    mMediaPlayer.start();
                    checkNotifyOnPlay();
                }
            }catch (IllegalStateException ex){
                handleErrorEncounted(0, 0);
            }
        }else if(mState == IPlayer.IDLE || mState == IPlayer.INITIALIZED){
            seekTo(0);
        }
    }

    private void checkNotifyOnPlay() {
        startNotify();
        notifyOnPlay(mFilePath);
    }

    @Override
    public void seekTo(int targetPosition) {
        //检查状态和文件,如果不合法,释放资源,然后向外报错
        //如果合法,检查当前的状态,如果是initial的状态,先标记为preparing,先做prepareAsyn的操作,
        //如果不是prepared的状态,那么直接做seekTo,在onPrepared中,设置状态为prepared的状态,然后调用started
        //Idle , need set datasource
        //Init --> Prepare
        //prepared --> run
        //run --> call start
        if(targetPosition < 0){
            targetPosition = 0;
        }
        mTargetPosition = targetPosition;
        if(TextUtils.isEmpty(mFilePath)){
            handleErrorEncounted(0,0);
            return;
        }
        if(mState == IPlayer.IDLE){
            try {
                //TODO: 是否还需要设置其他参数
                mMediaPlayer.setDataSource(mFilePath);
            } catch (IOException e) {
                e.printStackTrace();
                handleErrorEncounted(0, 0);
                return;
            }
            mState = IPlayer.INITIALIZED;
        }
        if(mState == IPlayer.INITIALIZED){
            mState = IPlayer.PREPARING;
            notifyOnPreparing(mFilePath);
            mMediaPlayer.prepareAsync();
        }else if(mState == IPlayer.PREPARING){
            //when prepared,remember seekTo
        }else if(mState == IPlayer.PREPARED){//just start,then check seekTo
            notifyOnPrepared(mFilePath);
            handlePrepared();
        }else if(mState == IPlayer.RUNNING){
            checkSeekPlay();
        }else{
            Log.e(TAG, "seekTo state not right, state:"+mState);
            handleErrorEncounted(0, 0);
        }
    }

    private void handlePrepared() {
        if(mState == IPlayer.PREPARED){
            mState = IPlayer.RUNNING;
            try{
                mMediaPlayer.start();
                checkNotifyOnPlay();
            }catch(IllegalStateException ex){
                ex.printStackTrace();
                handleErrorEncounted(0, 0);
                return;
            }
            checkSeekPlay();
        }else{
            Log.e(TAG, "handlPrepared state not right, currentState:"+mState);
            handleErrorEncounted(0, 0);
        }
    }

    private void checkSeekPlay() {
        if(mState == IPlayer.RUNNING){
            if(mTargetPosition >= 0){
                int targetPosition = mTargetPosition;
                mTargetPosition = -1;
                mMediaPlayer.seekTo(targetPosition);
            }else{
                if(mMediaPlayer.isPlaying()){
                    //do nothing
                }else{
                    mMediaPlayer.start();
                    checkNotifyOnPlay();
                }
            }
        }else{
            Log.e(TAG, "checkSeekPlay state not right, currentState:"+mState);
            handleErrorEncounted(0, 0);
        }
    }

    @Override
    public String getPlayPath() {
        return mFilePath;
    }

    @Override
    public int getDuration() {
        if(mState == IPlayer.PREPARED || mState == IPlayer.RUNNING){
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if(mState == IPlayer.PREPARED || mState == IPlayer.RUNNING || mState == IPlayer.INITIALIZED || mState == IPlayer.IDLE){
            return mMediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    public boolean isPlaying() {
        boolean result = false;
        if(mState == IPlayer.PREPARING){
            return false;
        }
        try{
            result = mMediaPlayer.isPlaying();
        }catch (IllegalStateException ex){
            ex.printStackTrace();
            handleErrorEncounted(0, 0);
        }
        return result;
    }

    @Override
    public int getState() {
        return mState;
    }

    private void handleErrorEncounted(int what, int extra){
        String filePath = mFilePath;
        init();
        notifyOnError(filePath, what, extra);
    }

    private void handleComplete(){
        String filePath = mFilePath;
        init();
        notifyOnComplete(filePath);
    }

    private void init(){
        if(mMediaPlayer != null){
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnInfoListener(null);
            mMediaPlayer.setOnSeekCompleteListener(null);
            //release current media player
            try{
                mMediaPlayer.release();
            }catch (IllegalStateException ex){
                ex.printStackTrace();
            }
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mState = IPlayer.IDLE;
        mTargetPosition = -1;
        mFilePath = null;
        stopNotify();
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if(mState == IPlayer.INITIALIZED || mState == IPlayer.PREPARING){
                mState = IPlayer.PREPARED;
                handlePrepared();
            }else{
                Log.e(TAG, "onPrepared state not right,mState:"+mState);
                handleErrorEncounted(0, 0);
            }
        }
    };

    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            handleSeekComplete();
        }
    };

    private void handleSeekComplete() {
        if(mState == IPlayer.PREPARED){
            handlePrepared();
        }else if(mState == IPlayer.RUNNING){
            checkSeekPlay();
        }else{
            //state not right
            Log.e(TAG, "onSeekComplte state not right, mState:"+mState);
            handleErrorEncounted(0, 0);
        }
    }


    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            handleErrorEncounted(what, extra);
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            handleComplete();
        }
    };

    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    private void startNotify(){
        mHandler.removeMessages(MSG_WHAT_NOTIFY_PROGRESS);
        mHandler.sendEmptyMessage(MSG_WHAT_NOTIFY_PROGRESS);
    }

    private void stopNotify(){
        mHandler.removeMessages(MSG_WHAT_NOTIFY_PROGRESS);
    }


    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_WHAT_NOTIFY_PROGRESS:
                    if(mState == IPlayer.RUNNING){
                        if(isPlaying()){
                            notifyOnProgressChanged(mFilePath, getCurrentPosition(), getDuration());
                            mHandler.removeMessages(MSG_WHAT_NOTIFY_PROGRESS);
                            mHandler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFY_PROGRESS, NOTIFY_PROGRESS_DELAY);
                        }
                    }
                    break;
            }
            return true;
        }
    });

}
