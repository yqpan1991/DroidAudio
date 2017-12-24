package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlay;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.media.MediaRecordWrapper;
import droidaudio.apollo.edus.com.droidaudio.multimedia.media.StatedMediaPlay;

/**
 * 学习使用系统Media相关的使用
 */
public class MediaV2Activity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MediaV2Activity.class.getSimpleName();
    private IRecord mRecord;
    private IPlay mPlay;
    private String mFilePath;
    private Handler mUiHandler = new Handler();
    private static final int MSG_CHECK_PLAY = 1;
    private boolean mNeedCheckPlay;
    private static final boolean LOG_ENABLE = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        findViewById(R.id.bt_play).setOnClickListener(this);
        findViewById(R.id.bt_stopPlay).setOnClickListener(this);
        findViewById(R.id.bt_pause).setOnClickListener(this);
        findViewById(R.id.bt_resume).setOnClickListener(this);

        initData();
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_record:
                handleRecord();
                break;
            case R.id.bt_stop:
                handleStopRecord();
                break;
            case R.id.bt_play:
                handlePlay();
                break;
            case R.id.bt_stopPlay:
                handleStopPlay();
                break;
            case R.id.bt_pause:
                handlePausePlay();
                break;
            case R.id.bt_resume:
                handleResumePlay();
                break;
        }
    }

    private void handleResumePlay() {
        if(mPlay != null){
            mPlay.resume();
        }
    }

    private void handlePausePlay() {
        if(mPlay != null){
            mPlay.pause();
        }
    }

    private void handleStopPlay() {
        mNeedCheckPlay = false;
        if(mPlay != null){
            mPlay.stop();
        }
    }

    private void handlePlay() {
        //注意: 由于recorder是异步返回的,所以如果录音还没有真正的结束,便开始播放,会导致异常
        if(mRecord != null){
            mNeedCheckPlay = true;
            mRecord.stopRecord();
            mRecord = null;
            return;
        }
        if(TextUtils.isEmpty(mFilePath)){
            Toast.makeText(this, "播放路径不存在", Toast.LENGTH_SHORT).show();
            handleStopPlay();
            return;
        }
        mNeedCheckPlay = false;
        if(mPlay == null){
            mPlay = new StatedMediaPlay();
            mPlay.addPlayListener(mPlayerListener);
            mPlay.play(mFilePath);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleStopRecord();
    }

    private void toastNotImplementYet(){
        Toast.makeText(this, "暂未实现", Toast.LENGTH_SHORT).show();
    }

    private void handleStopRecord() {
        if(mRecord != null){
            mRecord.stopRecord();
        }
    }

    private synchronized void handleRecord() {
        handleStopPlay();
        if(mRecord == null){
            mRecord = new MediaRecordWrapper(this.getApplicationContext());
            mRecord.addRecordListener(mRecordListener);
            mRecord.startRecord();
        }else{
            log("----started with not null");
        }
    }
    private IRecordListener mRecordListener = new IRecordListener() {
        @Override
        public void onStartRecord(String filePath) {
            mFilePath = filePath;
            log("onStartRecord:filePath:"+filePath);
        }

        @Override
        public void onStopRecord(String filePath) {
            if(mRecord != null){
                mRecord.removeRecordListener(mRecordListener);
                mRecord = null;
            }
            log("onStopRecordFinished,filePath:"+filePath);
            checkPlay();
        }

        @Override
        public void onRecordException(String filePath, int errorCode, String errorMsg) {
            if(mRecord != null){
                mRecord.removeRecordListener(mRecordListener);
                mRecord.stopRecord();
                mRecord = null;
            }
            log( "onRecordException, filePath:"+filePath+",errroCode:"+errorCode+",errorMsg:"+errorMsg);
            checkPlay();

        }
    };

    private IPlayerListener mPlayerListener = new IPlayerListener() {
        @Override
        public void onPreparing(String filePath) {
            log( "onPreparing , filePath:"+filePath);
        }

        @Override
        public void onPrepared(String filePath) {
            log( "onPrepared , filePath:"+filePath);
        }

        @Override
        public void onPause(String filePath) {
            log("onPause , filePath:"+filePath);
        }

        @Override
        public void onPlay(String filePath) {
            log("onPlay , filePath:"+filePath);
        }

        @Override
        public void onError(String filePath, int what, int extra) {
            log("onError , filePath:"+filePath);
            releasePlayer();
        }

        @Override
        public void onStopped(String filePath) {
            log("onStopped , filePath:"+filePath);
            releasePlayer();
        }

        @Override
        public void onComplete(String filePath) {
            log("onComplete , filePath:"+filePath);
            releasePlayer();
        }

        @Override
        public void onProgressChanged(String filePath, int curPosition, int duration) {
            log( "onProgressChanged , filePath:"+filePath);
        }
    };

    private void releasePlayer() {
        if(mPlay != null){
            mPlay.removePlayListener(mPlayerListener);
            mPlay = null;
        }
    }

    private void  checkPlay(){
        if(mNeedCheckPlay){
            handlePlay();
        }
    }

    private void log(String info){
        if(LOG_ENABLE){
            LogUtils.d(TAG, "[MediaV2Activity]"+info);
        }
    }

}
