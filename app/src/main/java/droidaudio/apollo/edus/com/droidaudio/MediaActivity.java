package droidaudio.apollo.edus.com.droidaudio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * 学习使用系统Media相关的使用
 */
public class MediaActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean mIsRecording;
    private MediaRecorder mRecorder;
    private String mFilePath;
    private MediaPlayer mPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        findViewById(R.id.bt_play).setOnClickListener(this);
        findViewById(R.id.bt_stopPlay).setOnClickListener(this);

        initData();
    }

    private void initData() {
        mFilePath = getFilesDir().getAbsolutePath() + File.separator + "record.amr";
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
        }
    }

    private void handleStopPlay() {
        if(mPlayer != null){
            mPlayer.stop();
        }
    }

    private void handlePlayRecord() {
        handleStopRecord();
        if(!new File(mFilePath).exists()){
            Toast.makeText(this.getApplicationContext(), "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFilePath);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            releasePlayer();
            return;
        }
        mPlayer.start();
    }

    private void releasePlayer() {
        if(mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void handleStopRecord() {
        mIsRecording = false;
        releaseRecorder();
    }

    private void handleRecord() {
        releasePlayer();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setOutputFile(mFilePath);
        try {
            mRecorder.prepare();
            mRecorder.start();
            mIsRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
            releaseRecorder();
        }
    }

    private void releaseRecorder() {
        if(mRecorder != null){
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
