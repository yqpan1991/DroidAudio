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

/**
 * 学习使用系统Media相关的使用
 */
public class MediaActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MediaActivity.class.getSimpleName();
    private MediaRecorder mRecorder;
    private String mFilePath;
    private MediaPlayer mPlayer;
    private MediaController mMediaController;

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
        mMediaController = MediaController.getInstance();
        mMediaController.init(this.getApplicationContext());
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
                releasePlayer();
                break;
            case R.id.bt_pause:
                handlePlayPause();
                break;
            case R.id.bt_resume:
                handlePlayResume();
                break;
        }
    }

    private void handlePlayResume() {
        //1. 检查前置条件
        //1.1. 停止录音
        //1.2  检查当前音乐播放器是否存在,如果存在,直接播放即可
        //1.3 如果不存在,检查是否有file存在,如果存在,使用play来播放,如果不存在,提示播放错误
        if(mPlayer != null){
            handleStopRecord();
            mPlayer.start();
        }else{
            handlePlayRecord();
        }
    }

    private void handlePlayPause() {
        //1. 检查千户之条件
        //3. 暂停
        if(mPlayer != null){
            mPlayer.pause();
        }
    }

    private void handlePlayRecord() {
        //1. 停止录音
        //2. 如果有上次的播放,释放上次的播放
        //3. 检查当前播放的有效性
        //4. 如果有效, 正常播放,无效,向外通知播放失败即可
        handleStopRecord();
        releasePlayer();
        if(TextUtils.isEmpty(mFilePath) || !new File(mFilePath).exists()){
            Toast.makeText(this.getApplicationContext(), "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFilePath);
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "player:onError");
                    return false;
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e(TAG, "player:onCompletion");
                    releasePlayer();
                }
            });
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            releasePlayer();
            return;
        }catch (IllegalStateException e){
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
        mMediaController.stopRecording();
    }

    private synchronized void handleRecord() {
        releasePlayer();
        mMediaController.startRecording(new IRecorderListener() {
            @Override
            public void onStart(String filePath) {
                mFilePath = filePath;
                Log.e(TAG, "onStart:"+filePath);
            }

            @Override
            public void onStop(String filePath, long duration) {
                Log.e(TAG, "onStop:filePath:"+filePath+",duration:"+duration);
            }

            @Override
            public void onError(int errorReason, int what, int extra) {
                Log.e(TAG, "onError,errorReason:"+errorReason+",what:"+what+",extra:"+extra);
            }
        });
    }

}
