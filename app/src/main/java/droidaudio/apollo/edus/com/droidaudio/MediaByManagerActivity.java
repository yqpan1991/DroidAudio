package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.multimedia.MediaManager;
import droidaudio.apollo.edus.com.droidaudio.multimedia.RecordType;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 * 学习使用系统Media相关的使用
 */
public class MediaByManagerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "["+MediaByManagerActivity.class.getSimpleName()+"]";
    private String mFilePath;
    private static final boolean LOG_ENABLE = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_by_manager);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        findViewById(R.id.bt_play).setOnClickListener(this);
        findViewById(R.id.bt_stopPlay).setOnClickListener(this);
        findViewById(R.id.bt_pause).setOnClickListener(this);
        findViewById(R.id.bt_resume).setOnClickListener(this);

        initData();
    }

    private void initData() {
        MediaManager.getInstance().addRecordListener(mRecordListener);
        MediaManager.getInstance().addPlayListener(mPlayerListener);
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
        MediaManager.getInstance().resume();
    }

    private void handlePausePlay() {
        MediaManager.getInstance().pause();
    }

    private void handleStopPlay() {
        MediaManager.getInstance().stop();
    }

    private void handlePlay() {
        if(TextUtils.isEmpty(mFilePath)){
            Toast.makeText(this, "播放路径不存在", Toast.LENGTH_SHORT).show();
            MediaManager.getInstance().stopRecord();
            MediaManager.getInstance().stop();
            return;
        }
        MediaManager.getInstance().play(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.getInstance().stop();
        MediaManager.getInstance().stopRecord();
    }

    private void handleStopRecord() {
        MediaManager.getInstance().stopRecord();
    }

    private synchronized void handleRecord() {
        MediaManager.getInstance().startRecord(RecordType.AMR);
    }
    private IRecordListener mRecordListener = new IRecordListener() {
        @Override
        public void onStartRecord(String filePath) {
            mFilePath = filePath;
            log("onStartRecord:filePath:"+filePath);
        }

        @Override
        public void onStopRecord(String filePath) {
            log("onStopRecordFinished,filePath:"+filePath);
        }

        @Override
        public void onRecordException(String filePath, int errorCode, String errorMsg) {
            log( "onRecordException, filePath:"+filePath+",errroCode:"+errorCode+",errorMsg:"+errorMsg);
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
        }

        @Override
        public void onStopped(String filePath) {
            log("onStopped , filePath:"+filePath);
        }

        @Override
        public void onComplete(String filePath) {
            log("onComplete , filePath:"+filePath);
        }

    };

    private void log(String info){
        if(LOG_ENABLE){
            LogUtils.d(LOG_TAG, info);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.getInstance().removePlayListener(mPlayerListener);
        MediaManager.getInstance().removeRecordListener(mRecordListener);
    }
}
