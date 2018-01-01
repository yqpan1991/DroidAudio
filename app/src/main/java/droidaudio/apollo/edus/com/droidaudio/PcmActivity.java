package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.PcmAudioRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.PcmAudioPlay;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 *
 * 查看系统audio相关的用法
 */
public class PcmActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();


    private PcmAudioRecord mPcmAudioRecord;
    private String mFilePath;
    private PcmAudioPlay mPcmAudioPlay;
    private boolean mNeedCheckPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_v2);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        findViewById(R.id.bt_play).setOnClickListener(this);
        findViewById(R.id.bt_stopPlay).setOnClickListener(this);
        findViewById(R.id.bt_pause).setOnClickListener(this);
        findViewById(R.id.bt_resume).setOnClickListener(this);
        findViewById(R.id.bt_stopPlay).setOnClickListener(this);

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
                handlePlayRecord();
                break;
            case R.id.bt_pause:
                handlePausePlay();
                break;
            case R.id.bt_resume:
                handleResumePlay();
                break;
            case R.id.bt_stopPlay:
                handleStopPlay();
                break;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleStopRecord();
        handleStopPlay();
    }

    private void handleResumePlay() {
        if(mPcmAudioPlay != null){
            mPcmAudioPlay.resume();
        }
    }

    private void handleStopPlay() {
        if(mPcmAudioPlay != null){
            mPcmAudioPlay.stop();
        }
    }

    private void handlePausePlay() {
        if(mPcmAudioPlay != null && mPcmAudioPlay.isPlaying()){
            mPcmAudioPlay.pause();
        }
    }

    private void handlePlayRecord() {
        if(mPcmAudioRecord != null){
            mPcmAudioRecord.stopRecord();
            mNeedCheckPlay = true;
        }
        if(mPcmAudioPlay == null && !TextUtils.isEmpty(mFilePath)){
            mPcmAudioPlay = new PcmAudioPlay();
            mPcmAudioPlay.addPlayListener(mIPlayerListener);
            mPcmAudioPlay.play(mFilePath);
        }
    }

    private void handleStopRecord() {
        if(mPcmAudioRecord != null){
            log("realStop");
            mPcmAudioRecord.stopRecord();
        }else{
            log("ignore stop");
        }
    }

    private void handleRecord() {
        if(mPcmAudioRecord == null){
            mPcmAudioRecord = new PcmAudioRecord(this);
            mPcmAudioRecord.addRecordListener(mIRecordListener);
            mPcmAudioRecord.startRecord();
        }
        handleStopPlay();
    }

    private IRecordListener mIRecordListener = new IRecordListener() {
        @Override
        public void onStartRecord(String filePath) {
            mFilePath = filePath;
            log("onRecordStart:"+filePath);
        }

        @Override
        public void onStopRecord(String filePath) {
            if(mPcmAudioRecord != null){
                mPcmAudioRecord.removeRecordListener(this);
                mPcmAudioRecord = null;
            }
            log("onStopRecord:"+filePath);
            checkPlay();
        }

        @Override
        public void onRecordException(String filePath, int errorCode, String errorMsg) {
            if(mPcmAudioRecord != null){
                mPcmAudioRecord.removeRecordListener(this);
                mPcmAudioRecord.stopRecord();
                mPcmAudioRecord = null;
            }
            log("onRecordException:"+filePath+",errorCode:"+errorCode+",errorMsg:"+errorMsg);
        }
    };

    private void checkPlay() {
        if(mNeedCheckPlay){
            mNeedCheckPlay = false;
            handlePlayRecord();
        }
    }

    private IPlayerListener mIPlayerListener = new IPlayerListener() {
        @Override
        public void onPreparing(String filePath) {
            log("onPreparing:"+filePath);
        }

        @Override
        public void onPrepared(String filePath) {
            log("onPrepared:"+filePath);
        }

        @Override
        public void onPause(String filePath) {
            log("onPause:"+filePath);
        }

        @Override
        public void onPlay(String filePath) {
            log("onPlay:"+filePath);
        }

        @Override
        public void onError(String filePath, int what, int extra) {
            log("onError:"+filePath+",what:"+what+",extra:"+extra);
            releasePlayer();
        }

        @Override
        public void onStopped(String filePath) {
            log("onStopped:"+filePath);
            releasePlayer();
        }

        @Override
        public void onComplete(String filePath) {
            log("onComplete:"+filePath);
            releasePlayer();
        }
    };

    private void releasePlayer() {
        if(mPcmAudioPlay != null){
            mPcmAudioPlay.removePlayListener(mIPlayerListener);
            mPcmAudioPlay = null;
        }
    }

    private void log(String info){
        if(TextUtils.isEmpty(info)){
            return;
        }
        LogUtils.d(TAG, info);
    }

}
