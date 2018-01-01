package droidaudio.apollo.edus.com.droidaudio.multimedia;

import android.content.Context;
import android.text.TextUtils;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlay;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.delegate.PlayerListenerDelegate;
import droidaudio.apollo.edus.com.droidaudio.multimedia.delegate.RecordListenerDelegate;

/**
 * 音频播放和音频录音的控制器
 * Created by panda on 2018/1/1.
 */

public class MediaManager implements IPlay{

    private final String TAG = this.getClass().getSimpleName();
    private final String LOG_TAG = "[MediaManager]";

    private volatile static MediaManager sInstance;
    private Context mContext;
    private boolean mInit;
    private PlayerListenerDelegate mPlayerListener;
    private RecordListenerDelegate mRecordListener;
    private Action mPostAction;
    private final int ACTION_TYPE_PLAY = 1;
    private final int ACTION_TYPE_RECORD = 2;
    private IPlay mPlayer;
    private IRecord mRecorder;


    public static MediaManager getInstance() {
        if (sInstance == null) {
            synchronized (MediaManager.class) {
                if (sInstance == null) {
                    sInstance = new MediaManager();
                }
            }
        }
        return sInstance;
    }

    private MediaManager() {
        mPlayerListener = new PlayerListenerDelegate(){
            @Override
            public void onError(String filePath, int what, int extra) {
                releasePlayer();
                super.onError(filePath, what, extra);
                checkPostAction();

            }

            @Override
            public void onStopped(String filePath) {
                releasePlayer();
                super.onStopped(filePath);
                checkPostAction();
            }

            @Override
            public void onComplete(String filePath) {
                releasePlayer();
                super.onComplete(filePath);
                checkPostAction();
            }
        };
        mRecordListener = new RecordListenerDelegate(){
            @Override
            public void onStopRecord(String filePath) {
                releaseRecorder();
                super.onStopRecord(filePath);
                checkPostAction();
            }


            @Override
            public void onRecordException(String filePath, int errorCode, String errorMsg) {
                releaseRecorder();
                super.onRecordException(filePath, errorCode, errorMsg);
                checkPostAction();
            }
        };
    }

    private void releaseRecorder() {
        if(mRecorder != null){
            mRecorder.removeRecordListener(mRecordListener);
            mRecorder = null;
        }
    }

    private void releasePlayer() {
        if(mPlayer != null){
            mPlayer.removePlayListener(mPlayerListener);
            mPlayer = null;
        }
    }

    private void checkPostAction() {
        if(mPostAction != null){
            Runnable runnable = mPostAction.postRun;
            int type = mPostAction.type;
            mPostAction = null;
            if(runnable != null){
                runnable.run();
            }
        }
    }

    public void init(Context context){
        if(!mInit){
            mInit = true;
            mContext = context;
            if(mContext == null){
                throw new RuntimeException("MediaManager init params cannot be null");
            }
            RecordFactory.getInstance().init(mContext);
            PlayFactory.getInstance().init(mContext);
        }
    }

    public void startRecord(final RecordType recordType){
        //如果在播放中,先停止
        if(mPlayer != null){
            mPostAction = new Action();
            mPostAction.type = ACTION_TYPE_RECORD;
            mPostAction.postRun = new Runnable() {
                @Override
                public void run() {
                    startRecordInner(recordType);
                }
            };
            mPlayer.stop();
        }else{
            startRecordInner(recordType);
        }
    }

    private void startRecordInner(RecordType recordType) {
        if(mPlayer != null){
            throw new RuntimeException("startRecordInner, but player is not null");
        }
        if(mRecorder != null){
            return;
        }
        mRecorder = RecordFactory.getInstance().getRecorder(recordType);
        if(mRecorder == null){
            logInfo("[startRecordInner] but recorder is not null, ignore");
            return;
        }
        mRecorder.addRecordListener(mRecordListener);
        mRecorder.startRecord();
    }

    public void stopRecord(){
        if(mRecorder != null){
            mRecorder.stopRecord();
        }
        if(mPostAction != null && mPostAction.type == ACTION_TYPE_RECORD){
            mPostAction = null;
        }
    }
    public boolean isRecording(){
        if(mRecorder != null){
            return mRecorder.isRecording();
        }
        return false;
    }

    /**
     * 录音开始后,才能获取到录音的路径
     * @return
     */
    public String getRecordPath(){
        if(mRecorder != null){
            return mRecorder.getRecordPath();
        }
        return null;
    }

    public void addRecordListener(IRecordListener recordListener){
        mRecordListener.addRecordListener(recordListener);
    }
    public void removeRecordListener(IRecordListener recordListener){
        mRecordListener.removeRecordListener(recordListener);
    }

    @Override
    public void play(String url) {
        play(url, 0);
    }

    @Override
    public void play(final String url, final int pos) {
        //如果在播放中,需要判断播放的url,是否相同,并且需要判断是否在录音中
        if(mRecorder != null){
            mPostAction = new Action();
            mPostAction.type = ACTION_TYPE_PLAY;
            mPostAction.postRun = new Runnable(){

                @Override
                public void run() {
                    startPlayInner(url, pos);
                }
            };
            mRecorder.stopRecord();
        }else{
            startPlayInner(url, pos);
        }
    }

    private void startPlayInner(final String url, final int pos) {
        if(TextUtils.isEmpty(url)){
            return;
        }
        if(mPlayer != null){
            if(TextUtils.equals(url, mPlayer.getPlayPath())){
                mPlayer.resume();
            }else{
                mPostAction = new Action();
                mPostAction.type = ACTION_TYPE_PLAY;
                mPostAction.postRun = new Runnable() {
                    @Override
                    public void run() {
                        startCurrentPlayer(url, pos);
                    }
                };
                mPlayer.stop();
            }
        }else{
            startCurrentPlayer(url, pos);
        }
    }

    private void startCurrentPlayer(String url, int pos) {
        if(mPlayer != null){
            throw new RuntimeException("startCurrentPlayer mPlayer cannot be not null");
        }
        mPlayer = PlayFactory.getInstance().getPlayerByUrl(url);
        if(mPlayer == null){
            return;
        }
        mPlayer.addPlayListener(mPlayerListener);
        mPlayer.play(url, pos);
    }

    @Override
    public void pause() {
        if(mPlayer != null){
            mPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if(mPlayer != null){
            mPlayer.stop();
        }
        if(mPostAction != null && mPostAction.type == ACTION_TYPE_PLAY){
            mPostAction = null;
        }
    }

    @Override
    public void resume() {
        if(mPlayer != null){
            mPlayer.resume();
        }
    }

    @Override
    public void seekTo(int targetPosition) {
        if(mPlayer != null){
            mPlayer.seekTo(targetPosition);
        }
    }

    @Override
    public String getPlayPath() {
        if(mPlayer != null){
            return mPlayer.getPlayPath();
        }
        return null;
    }

    @Override
    public int getDuration() {
        if(mPlayer != null){
            return mPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(mPlayer != null){
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if(mPlayer != null){
            return mPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public int getState() {
        if(mPlayer != null){
            return mPlayer.getState();
        }
        return 0;
    }

    @Override
    public void addPlayListener(IPlayerListener listener) {
        mPlayerListener.addIPlayerListener(listener);
    }

    @Override
    public void removePlayListener(IPlayerListener listener) {
        mPlayerListener.removeIPlayerListener(listener);
    }

    private class Action{
        int type;
        Runnable postRun;
    }

    private String getActionTypeString(int type){
        if(type == ACTION_TYPE_PLAY){
            return "[play]";
        }else if(type == ACTION_TYPE_RECORD){
            return "[record]";
        }
        return "[unknown]";
    }

    private void logInfo(String info){
        if(TextUtils.isEmpty(info)){
            return;
        }
        LogUtils.v(LOG_TAG, info);
    }

}
