package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.OpusAudioRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.OpusAudioTrack;

/**
 *
 * 查看系统audio相关的用法
 */
public class OpusActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();


    private OpusAudioRecord mOpusAudioRecord;
    private String mFilePath;
    private OpusAudioTrack mOpusAudioTrack;
    private boolean mNeedCheckPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opus);
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
                handlePause();
                break;
            case R.id.bt_resume:
                handleResume();
                break;


        }
    }

    private void handleResume() {
        if(mOpusAudioTrack != null){
            mOpusAudioTrack.resume();
        }
    }

    private void handlePause() {
        if(mOpusAudioTrack != null){
            mOpusAudioTrack.pause();
        }
    }

    private void handleStopPlay() {
        if(mOpusAudioTrack != null){
            mOpusAudioTrack.stop();
        }
    }

    private void handlePlay() {
        if(TextUtils.isEmpty(mFilePath)){
            Toast.makeText(this, "filePath is null, cannot play:", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mOpusAudioRecord != null){
            mNeedCheckPlay = true;
            mOpusAudioRecord.stopRecord();
            return;
        }
        if(mOpusAudioTrack == null){
            mOpusAudioTrack = new OpusAudioTrack();
            mOpusAudioTrack.addPlayListener(mIPlayListener);
            mOpusAudioTrack.play(mFilePath);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleStopRecord();
        handleStopPlay();
    }


    private void handleStopRecord() {
        if(mOpusAudioRecord != null){
            log("realStop");
            mOpusAudioRecord.stopRecord();
        }else{
            log("ignore stop");
        }
    }

    private void handleRecord() {
        if(mOpusAudioTrack != null){
            mOpusAudioTrack.stop();
        }

        if(mOpusAudioRecord == null){
            mOpusAudioRecord = new OpusAudioRecord(this);
            mOpusAudioRecord.addRecordListener(mIRecordListener);
            mOpusAudioRecord.startRecord();
        }
    }

    private IRecordListener mIRecordListener = new IRecordListener() {
        @Override
        public void onStartRecord(String filePath) {
            mFilePath = filePath;
            log("onRecordStart:"+filePath);
        }

        @Override
        public void onStopRecord(String filePath) {
            if(mOpusAudioRecord != null){
                mOpusAudioRecord.removeRecordListener(this);
                mOpusAudioRecord = null;
                checkPlay();
            }
            log("onStopRecord:"+filePath);
        }

        @Override
        public void onRecordException(String filePath, int errorCode, String errorMsg) {
            if(mOpusAudioRecord != null){
                mOpusAudioRecord.removeRecordListener(this);
                mOpusAudioRecord.stopRecord();
                mOpusAudioRecord = null;
            }
            log("onRecordException:"+filePath+",errorCode:"+errorCode+",errorMsg:"+errorMsg);
        }
    };

    private void checkPlay() {
        if(mNeedCheckPlay){
            mNeedCheckPlay = false;
            handlePlay();
        }
    }

    private IPlayerListener mIPlayListener = new IPlayerListener() {
        @Override
        public void onPreparing(String filePath) {
            log("onPreparing:filePath:"+filePath);
        }

        @Override
        public void onPrepared(String filePath) {
            log("onPrepared:filePath:"+filePath);
        }

        @Override
        public void onPause(String filePath) {
            log("onPause:filePath:"+filePath);
        }

        @Override
        public void onPlay(String filePath) {
            log("onPlay:filePath:"+filePath);
            if(mOpusAudioTrack != null){
                log("duration:"+mOpusAudioTrack.getDuration()+",currentPos:"+mOpusAudioTrack.getCurrentPosition());
            }
        }

        @Override
        public void onError(String filePath, int what, int extra) {
            log("onError:filePath:"+filePath+",what:"+what+",extra:"+extra);
            if(mOpusAudioTrack != null){
                mOpusAudioTrack.removePlayListener(mIPlayListener);
                mOpusAudioTrack = null;
            }
        }

        @Override
        public void onStopped(String filePath) {
            log("onStopped:filePath:"+filePath);
            if(mOpusAudioTrack != null){
                mOpusAudioTrack.removePlayListener(mIPlayListener);
                mOpusAudioTrack = null;
            }
        }

        @Override
        public void onComplete(String filePath) {
            log("onComplete:filePath:"+filePath);
            if(mOpusAudioTrack != null){
                mOpusAudioTrack.removePlayListener(mIPlayListener);
                mOpusAudioTrack = null;
            }
        }

    };



    private void log(String info){
        if(TextUtils.isEmpty(info)){
            return;
        }
        LogUtils.e(TAG, info);
    }

}
