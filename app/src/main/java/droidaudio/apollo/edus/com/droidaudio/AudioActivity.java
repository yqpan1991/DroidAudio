package droidaudio.apollo.edus.com.droidaudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import droidaudio.apollo.edus.com.droidaudio.audio.DroidAudioRecorder;
import droidaudio.apollo.edus.com.droidaudio.audio.DroidAudioTrack;
import droidaudio.apollo.edus.com.droidaudio.audio.IDroidAudioRecorder;
import droidaudio.apollo.edus.com.droidaudio.audio.IDroidAudioTrack;

/**
 * 查看系统audio相关的用法
 */
public class AudioActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    private String mFilePath;
    private DroidAudioRecorder mRecorder;
    private DroidAudioTrack mTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
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
        mFilePath = getFilesDir().getAbsolutePath() + File.separator + "record.pcm";
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
            case R.id.bt_stopPlay:
                handleStopPlay();
                break;
            case R.id.bt_resume:
                handleResume();
                break;
            case R.id.bt_pause:
                handlePause();
                break;
        }
    }

    private void handlePause() {
        if(mTrack != null){
            mTrack.pause();
        }
    }

    private void handleResume() {
        if(mTrack != null){
            mTrack.resume();
        }
    }

    private void handleStopPlay() {
        if(mTrack != null){
            mTrack.stop();
            mTrack = null;
        }
    }

    private void handlePlayRecord() {
        handleStopRecord();
        handleStopPlay();
        if(TextUtils.isEmpty(mFilePath)){
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mTrack == null){
            mTrack = new DroidAudioTrack();
            mTrack.setOnPlayStateChangedListener(new IDroidAudioTrack.OnPlayStateChangedListener() {
                @Override
                public void onPlayStateChanged(int playState) {
                    if(playState == IDroidAudioTrack.PLAY_STATE_STOP){
                        mTrack = null;
                    }
                }
            });
            mTrack.play(mFilePath);
        }
    }


    private void handleStopRecord() {
        if(mRecorder != null){
            mRecorder.stopRecord();
            mRecorder = null;
        }
    }

    private void handleRecord() {
        handleStopPlay();
        if(mRecorder == null){
            mRecorder = new DroidAudioRecorder(this);
            mRecorder.setOnDroidAudioRecordListener(new IDroidAudioRecorder.OnDroidAudioRecordListener() {
                @Override
                public void onRecordStart(String filePath) {
                    Log.e(TAG,"onRecordStart:"+filePath);
                    mFilePath = filePath;
                }

                @Override
                public void onRecordStop(String filePath) {
                    Log.e(TAG,"onRecordStop:"+filePath);
                }

                @Override
                public void onRecordError(IDroidAudioRecorder recorder, int what, int extra) {
                    Log.e(TAG,"onRecordError: what:"+what+",extra"+extra);
                }
            });
        }
        if(!mRecorder.isRecoding()){
            mRecorder.startRecord();
        }
    }

}
