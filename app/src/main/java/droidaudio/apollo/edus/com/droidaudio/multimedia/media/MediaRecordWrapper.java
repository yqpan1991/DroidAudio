package droidaudio.apollo.edus.com.droidaudio.multimedia.media;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;

import com.edus.apollo.common.utils.log.LogUtils;

import java.io.File;
import java.io.IOException;

import droidaudio.apollo.edus.com.droidaudio.utils.MainLooper;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.RecordUtils;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.BaseRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 * 自己记录状态,不主动进行停止,让外面操作,保证数据流的唯一
 * <br>踩过的坑<br>
 * <p>
 * <p>
 * 1. 在开始录音后,快速的结束录音时,发现总是会回调onException,发现的现象: 在设置了onError,并且在stop时没有移除setOnErrorListener,
 * 然后开始MediaRecorder的stop()和release()操作时,会发生这样的情况
 * 解决办法: 因为我们的MediaRecorder只是使用一次,因而在stop()时,应该先移除回调,再进行其他的操作,
 * 可能是MediaRecorder的状态有问题,所以我们也可以不调用MediaRecorder的stop()操作,
 * 直接release(),但是看google的状态机,这样做不好,所以还是先移除监听,再stop,然后release是最好的做法
 * <p>
 * </p>
 * Created by panda on 2017/12/17.
 */

public class MediaRecordWrapper extends BaseRecord {

    private static final boolean LOG_ENABLE = true;

    private String TAG = this.getClass().getSimpleName();

    private String mFilePath;
    private Context mContext;
    private MediaRecorder mRecorder;
    private boolean mIsRecording;
    private boolean mHasError;

    public MediaRecordWrapper(Context context) {
        if (context == null) {
            throw new RuntimeException("MediaRecordWrapper context cannot be null");
        }
        mContext = context.getApplicationContext();
    }

    @Override
    public void startRecord() {
        //检测是否合法,开始了录制,则不再开始
        //开启线程
        log("startRecord()");
        if (isRecording()) {
            log("---startRecord when is recording, just return");
            return;
        }
        mIsRecording = true;
        RecordUtils.getRecordCmdTask().start(new Runnable() {
            @Override
            public void run() {
                startRecordInner();
            }
        });
    }

    private void startRecordInner() {
        //1. 生成文件的路径
        //2. 构造录音的参数
        //3. 开始录制
        String filePath = makeFilePath();
        if (TextUtils.isEmpty(filePath)) {//保护文件路径的操作,正常来讲是肯定存在的
            mHasError = true;
            notifyOnRecordException(filePath, IRecordListener.ERROR_FILE_GENERATE_ERROR, null);
            return;
        }
        mFilePath = filePath;
        //recorder should be created has Looper running
        mRecorder = new MediaRecorder();
        //1. 设置audioSource --> Initial --> Initialized
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//这里最好使用MIC,使用VOICE_RECOGNITION在小米MIX2上会存在问题
        //2.设置OutputFormat, Initialized --> DataSourceConfigured
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //3. 设置其他的选项
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setOnErrorListener(mOnErrorListener);
        mRecorder.setOnInfoListener(mOnInfoListener);
        try {
            //4. 调用prepare, DataSourceConfigured --> Prepared
            mRecorder.prepare();
            //5. 调用start, Prepared --> Recording
            mRecorder.start();
            //so call started recording
            notifyOnStartRecord(mFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            mHasError = true;
            log("IOException:" + e.toString());
            unbindInnerRecorderListener();
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_RECORD_START_EXCEPTION, " start exception:" + e.toString());
        } catch (RuntimeException e) {
            e.printStackTrace();
            mHasError = true;
            log("RuntimeException:" + e.toString());
            unbindInnerRecorderListener();
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_RECORD_START_EXCEPTION, " start exception:" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            mHasError = true;
            log("Exception:" + e.toString());
            unbindInnerRecorderListener();
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_RECORD_START_EXCEPTION, " start exception:" + e.toString());
        }
    }

    private MediaRecorder.OnErrorListener mOnErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            mHasError = true;
            log("onError");
            unbindInnerRecorderListener();
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_INNER_AUDIO_RECORD, "inner audio record error,code:" + what + ",extra:" + extra);
        }
    };

    private void unbindInnerRecorderListener() {
        if (mRecorder != null) {
            mRecorder.setOnErrorListener(null);
            mRecorder.setOnInfoListener(null);
        }
    }

    private MediaRecorder.OnInfoListener mOnInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            log("onInfo:what:" + what + ",extra:" + extra);
        }
    };


    @Override
    public void stopRecord() {
        //检测合法性,如果没有开始,则直接返回
        log("stopRecord()");
        if (!isRecording()) {
            log("---stopRecord when is recording, just return");
            return;
        }
        RecordUtils.getRecordCmdTask().start(new Runnable() {
            @Override
            public void run() {
                stopRecordInner();
            }
        });
    }

    private void stopRecordInner() {
        //1. 如果开启了录音,关闭录音即可
        //2. 如果在录制过程中,出现了错误,不再通知onRecordStop,即错误和异常二者只通知一次
        //3. 其他情况下,通知结束
        log("stopRecordInner()");
        boolean realReleased = releaseRecordInner();//由于stopRecord 可能会被外面多次调用,并且并且异步操作,因而需要做校验,是否是真正的释放了,这样才能向外通知
        if (realReleased) {
            checkNotifyRecordStop();
            MainLooper.instance().post(new Runnable() {
                @Override
                public void run() {
                    mIsRecording = false;
                }
            });
        }
    }


    private void checkNotifyRecordStop() {
        if (mHasError) {
            return;
        }
        notifyOnStopRecord(mFilePath);
    }

    /**
     * 释放内部的录音器
     * @return true 存在播放器并且真正的释放了 false 不存在播放器的信息
     */
    private boolean releaseRecordInner() {
        if (mRecorder != null) {
            //必须先移除监听,否则在start()后,快速的stop()时,MediaRecorder的错误状态信息可能会回调出来
            unbindInnerRecorderListener();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            return true;
        }
        log("stopped where recorder not exists");
        return false;
    }

    @Override
    public boolean isRecording() {
        //马上开始时,便设置为了开始
        //录音真正的结束后,才设置为了结束
        return mIsRecording;
    }

    @Override
    public String getRecordPath() {
        return mFilePath;
    }

    private String makeFilePath() {
        if (mContext == null) {
            return null;
        }
        String dir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = mContext.getExternalCacheDir().getAbsolutePath();
        } else {
            dir = mContext.getFilesDir().getAbsolutePath();
        }
        File file = new File(dir);
        file.mkdirs();
        return dir + File.separator + "record-" + System.currentTimeMillis() + ".amr";
    }

    private void log(String info) {
        if (LOG_ENABLE) {
            LogUtils.d(TAG, "[MediaRecordWrapper]" + info);
        }
    }
}
