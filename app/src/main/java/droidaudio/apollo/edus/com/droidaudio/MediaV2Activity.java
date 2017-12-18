package droidaudio.apollo.edus.com.droidaudio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import droidaudio.apollo.edus.com.droidaudio.media.IRecorderListener;
import droidaudio.apollo.edus.com.droidaudio.media.MediaController;
import droidaudio.apollo.edus.com.droidaudio.record.audio.IRecord;
import droidaudio.apollo.edus.com.droidaudio.record.audio.IRecordListener;
import droidaudio.apollo.edus.com.droidaudio.record.audio.MediaRecordWrapper;

/**
 * 学习使用系统Media相关的使用
 */
public class MediaV2Activity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MediaV2Activity.class.getSimpleName();
    private IRecord mRecord;
    private String mFilePath;

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
                toastNotImplementYet();
                break;
            case R.id.bt_stopPlay:
                toastNotImplementYet();
                break;
            case R.id.bt_pause:
                toastNotImplementYet();
                break;
            case R.id.bt_resume:
                toastNotImplementYet();
                break;
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
        if(mRecord == null){
            mRecord = new MediaRecordWrapper(this.getApplicationContext());
            mRecord.addRecordListener(mListener);
            mRecord.startRecord();
        }else{
            Log.e(TAG, "----started with not null");
        }
    }
    private IRecordListener mListener = new IRecordListener() {
        @Override
        public void onStartRecord(String filePath) {
            mFilePath = filePath;
            Log.e(TAG, "onStartRecord:filePath:"+filePath);
        }

        @Override
        public void onStopRecord(String filePath) {
            if(mRecord != null){
                mRecord.removeRecordListener(mListener);
                mRecord = null;
            }
            Log.e(TAG, "onStopRecordFinished,filePath:"+filePath);
        }

        @Override
        public void onRecordException(String filePath, int errorCode, String errorMsg) {
            if(mRecord != null){
                mRecord.removeRecordListener(mListener);
                mRecord.stopRecord();
                mRecord = null;
            }
            Log.e(TAG, "onRecordException, filePath:"+filePath+",errroCode:"+errorCode+",errorMsg:"+errorMsg);

        }
    };

}
