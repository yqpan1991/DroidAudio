package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.OpusAudioRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.PcmAudioRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 *
 * 查看系统audio相关的用法
 */
public class OpusActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();


    private OpusAudioRecord mOpusAudioRecord;
    private String mFilePath;


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

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleStopRecord();
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



    private void log(String info){
        if(TextUtils.isEmpty(info)){
            return;
        }
        LogUtils.d(TAG, info);
    }

}
