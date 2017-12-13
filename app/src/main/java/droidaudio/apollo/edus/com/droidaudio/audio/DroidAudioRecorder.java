package droidaudio.apollo.edus.com.droidaudio.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 封装android使用的audio record
 * Created by PandaPan on 2016/11/13.
 */

public class DroidAudioRecorder implements IDroidAudioRecorder {
    private final String TAG = this.getClass().getSimpleName();
    private int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private int SAMPLE_RATE = 16000;
    private int CHANNEL_IN_COUNT = AudioFormat.CHANNEL_IN_STEREO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private OnDroidAudioRecordListener mOnDroidAudioRecorderListener;
    private AudioRecord mAudioRecord;
    private int mRecordBufferSize;
    private String mFilePath;
    private Context mContext;
    private Handler mUIHandler;
    private Thread mRecordingThread;


    public DroidAudioRecorder(Context context){
        mContext = context;
        mUIHandler = new Handler();
    }

    @Override
    public void startRecord() {
        stopRecord();
        if(!isAudioRecordValid()){
            createFilePath();
            if(TextUtils.isEmpty(mFilePath)){
                if(mOnDroidAudioRecorderListener != null){
                    mOnDroidAudioRecorderListener.onRecordError(this, IDroidAudioRecorder.ERROR_WHAT_START_RECORD, IDroidAudioRecorder.ERROR_EXTRA_EMPTY_FILE_PATH);
                }
                return;
            }
            mRecordBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_COUNT, AUDIO_FORMAT);
            mAudioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_IN_COUNT, AUDIO_FORMAT, mRecordBufferSize);
            mAudioRecord.startRecording();
            if(mOnDroidAudioRecorderListener != null){
                mOnDroidAudioRecorderListener.onRecordStart(mFilePath);
            }
            mRecordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeDataToFile();
                }
            });
            mRecordingThread.start();
        }
    }

    private void writeDataToFile() {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(mFilePath));
            byte[] bufferBytes = new byte[mRecordBufferSize];
            while(isAudioRecordValid() && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
                int read = mAudioRecord.read(bufferBytes, 0, mRecordBufferSize);
                if(read >  0){
                    bos.write(bufferBytes, 0, read);
                }else{
                    break;
                }
            }
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            postError(IDroidAudioRecorder.ERROR_WHAT_DURING_RECORD, IDroidAudioRecorder.ERROR_EXTRA_WRITE_FILE_ERROR);
        }finally {
            postRunnable(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                }
            });
        }
    }

    private void createFilePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        mFilePath = mContext.getFilesDir().getAbsolutePath() + File.separator + "record"+sdf.format(new Date())+".pcm";
        File file = new File(mFilePath);
        if(file.exists()){
            file.delete();
        }
    }

    @Override
    public void stopRecord() {
        if(isAudioRecordValid()){
            mAudioRecord.stop();
            mAudioRecord.release();
            mRecordingThread = null;
            if(mOnDroidAudioRecorderListener != null){
                mOnDroidAudioRecorderListener.onRecordStop(mFilePath);
            }
            mFilePath = null;
        }
    }

    @Override
    public boolean isRecoding() {
        return isAudioRecordValid() && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    private boolean isAudioRecordValid(){
        return mAudioRecord != null && mAudioRecord.getState() == AudioTrack.STATE_INITIALIZED;
    }

    @Override
    public void setOnDroidAudioRecordListener(OnDroidAudioRecordListener listener) {
        mOnDroidAudioRecorderListener = listener;
    }

    @Override
    public OnDroidAudioRecordListener getOnDroidAudioRecordListener() {
        return mOnDroidAudioRecorderListener;
    }

    private void postError(final int what, final int extra){
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mOnDroidAudioRecorderListener != null){
                    mOnDroidAudioRecorderListener.onRecordError(DroidAudioRecorder.this, what, extra);
                }
            }
        });
    }

    private void postRunnable(Runnable runnable){
        if(runnable != null){
            mUIHandler.post(runnable);
        }
    }
}
