package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.edus.apollo.common.utils.log.LogUtils;

import droidaudio.apollo.edus.com.droidaudio.record.audio.PcmRecordWrapper;
import droidaudio.apollo.edus.com.droidaudio.record.audio.IRecordListener;

/**
 *
 * 查看系统audio相关的用法
 */
public class PcmV2Activity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();


    private PcmRecordWrapper mPcmRecordWrapper;
    private String mFilePath;


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
        }
    }

    private void handleStopRecord() {
        if(mPcmRecordWrapper != null){
            log("realStop");
            mPcmRecordWrapper.stopRecord();
        }else{
            log("ignore stop");
        }
    }

    private void handleRecord() {
        if(mPcmRecordWrapper == null){
            mPcmRecordWrapper = new PcmRecordWrapper(this);
            mPcmRecordWrapper.addRecordListener(mIRecordListener);
            mPcmRecordWrapper.startRecord();
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
            if(mPcmRecordWrapper != null){
                mPcmRecordWrapper.removeRecordListener(this);
                mPcmRecordWrapper = null;
            }
            log("onStopRecord:"+filePath);
        }

        @Override
        public void onRecordException(String filePath, int errorCode, String errorMsg) {
            if(mPcmRecordWrapper != null){
                mPcmRecordWrapper.removeRecordListener(this);
                mPcmRecordWrapper.stopRecord();
                mPcmRecordWrapper = null;
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
