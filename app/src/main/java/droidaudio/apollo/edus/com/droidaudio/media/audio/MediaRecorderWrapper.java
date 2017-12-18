package droidaudio.apollo.edus.com.droidaudio.media.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import droidaudio.apollo.edus.com.droidaudio.Utils.MainLooper;
import droidaudio.apollo.edus.com.droidaudio.media.IRecorder;
import droidaudio.apollo.edus.com.droidaudio.media.IRecorderListener;

/**
 * Created by PandaPan on 2017/2/6.
 */

public class MediaRecorderWrapper implements IRecorder {
    private static final String TAG = MediaRecorderWrapper.class.getSimpleName();
    private IRecorderListener mRecorderListener;
    private Context mContext;
    private boolean mIsRecording;
    private MediaRecorder mRecorder;
    private String mFilePath;

    public MediaRecorderWrapper(Context context, IRecorderListener listener){
        mContext = context;
        mRecorderListener = listener;
    }

    @Override
    public void startRecording() {
        if(mIsRecording){
            return;
        }
        mIsRecording = true;
        //TODO: 参数需要外部注入
        mRecorder = new MediaRecorder();
        //1. 设置audioSource --> Initial --> Initialized
        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        //2.设置OutputFormat, Initialized --> DataSourceConfigured
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //3. 设置其他的选项
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setAudioEncodingBitRate(16);
        mFilePath = makeFilePath();
        if(TextUtils.isEmpty(mFilePath)){
            notifyErrorEncounted(IRecorderListener.STOP_REASON_OUTTER_NO_FILE, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
            stopRecordingInner(IRecorderListener.STOP_REASON_OUTTER_NO_FILE, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
            return;
        }
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                notifyErrorEncounted(IRecorderListener.STOP_REASON_INNER_RECORDING, what, extra);
                stopRecordingInner(IRecorderListener.STOP_REASON_INNER_RECORDING, what, extra);
            }
        });
        try {
            //4. 调用prepare, DataSourceConfigured --> Prepared
            mRecorder.prepare();
            //5. 调用start, Prepared --> Recording
            mRecorder.start();
            //so call started recording
            notifyStartRecording(mFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            notifyErrorEncounted(IRecorderListener.STOP_REASON_INNER_START, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
            stopRecordingInner(IRecorderListener.STOP_REASON_INNER_START, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
        }catch (RuntimeException e){
            e.printStackTrace();
            notifyErrorEncounted(IRecorderListener.STOP_REASON_INNER_START, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
            stopRecordingInner(IRecorderListener.STOP_REASON_INNER_START, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
        }
    }

    @Override
    public void stopRecording() {
        stopRecordingInner(IRecorderListener.STOP_REASON_OUTTER_STOP, IRecorderListener.STOP_REASON_WHAT_NORMAL, IRecorderListener.STOP_REASON_EXTRA_NORMAL);
    }

    @Override
    public boolean isRecording() {
        return mIsRecording;
    }

    private void stopRecordingInner(int stopReason, int what, int extra){
        if(mIsRecording){
            Log.e(TAG,  "stopReason"+stopReason+",what:"+what+",extra:"+extra);
            mIsRecording = false;
            notifyStopRecording(mFilePath);
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

    private String makeFilePath() {
        if(mContext == null){
            return null;
        }
        return mContext.getFilesDir().getAbsolutePath() + File.separator + "record-" + System.currentTimeMillis() + ".amr";
    }

    private void notifyStopRecording(final String filePath) {
        Log.e(TAG, "stopRecoding:filePath:"+filePath);
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                if(mRecorderListener != null){
                    mRecorderListener.onStop(filePath,0);
                }
            }
        });
    }

    private void notifyErrorEncounted(final int stopReason, final int what, final int extra) {
        Log.e(TAG,  "errorEncounted:stopReason"+stopReason+",what:"+what+",extra:"+extra);
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                if(mRecorderListener != null){
                    mRecorderListener.onError(stopReason, what, extra);
                }
            }
        });
    }

    private void notifyStartRecording(final String filePath) {
        Log.e(TAG, "startRecoding:filePath:"+filePath);
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                if(mRecorderListener != null){
                    mRecorderListener.onStart(filePath);
                }
            }
        });

    }
}
