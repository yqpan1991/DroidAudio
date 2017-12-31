package droidaudio.apollo.edus.com.droidaudio.multimedia.audio;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.text.TextUtils;

import com.edus.apollo.common.utils.log.LogUtils;

import java.io.File;
import java.util.concurrent.Semaphore;

import droidaudio.apollo.edus.com.droidaudio.Utils.MainLooper;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.BaseRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.RecordUtils;

/**
 * Created by panda on 2017/12/24.
 */

public abstract class AudioRecordWrapper extends BaseRecord {

    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private boolean mHasError;
    private AudioRecord mAudioRecord;
    private boolean mIsRecording;
    private String mFilePath;
    private Semaphore mFileWriteSemaphore;
    private boolean LOG_ENABLE = true;

    public AudioRecordWrapper(Context context){
        if (context == null) {
            throw new RuntimeException("MediaRecordWrapper context cannot be null");
        }
        mContext = context.getApplicationContext();
        mFileWriteSemaphore = new Semaphore(0);
    }

    @Override
    public void startRecord() {
        /**
         * - 是否在录制中,如果在,不再录制
         - 生成文件路径
         - AudioTrack的构造
         - 开始录制
         - 异步的读取AudioTrack中的数据,将数据写入到文件中
         */
        if(isRecording()){
            return;
        }
        mIsRecording = true;
        logInfo("startRecording");
        RecordUtils.getRecordCmdTask().start(new Runnable() {
            @Override
            public void run() {
                startRecordInner();
            }
        });


    }

    private void startRecordInner() {
        /**
         * - 生成文件路径
         - AudioTrack的构造
         - 开始录制
         - 异步的读取AudioTrack中的数据,将数据写入到文件中
         */
        logInfo("startRecordingInner()");
        String filePath = makeFilePath();
        if (TextUtils.isEmpty(filePath)) {//保护文件路径的操作,正常来讲是肯定存在的
            logInfo("startRecord filePath generate error");
            mHasError = true;
            notifyOnRecordException(filePath, IRecordListener.ERROR_FILE_GENERATE_ERROR, null);
            return;
        }
        mFilePath = filePath;
        mAudioRecord = initAudioRecord();
        if(mAudioRecord == null){
            throw new RuntimeException("AudioRecord must be init");
        }
        try{
            mAudioRecord.startRecording();
        }catch (Exception ex){
            ex.printStackTrace();
            logInfo("startRecord error, no record permission");
            mHasError = true;
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_NO_RECORD_PERMISSION, "no record permission 1");
            return;
        }
        if(!isAudioRecordRecording()){
            logInfo("startRecord error, no record permission");
            mHasError = true;
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_NO_RECORD_PERMISSION, "no record permission 2");
            return;
        }
        //开启读写的线程,写入数据即可
        RecordUtils.getSingleRealRecordTask().start(new Runnable() {
            @Override
            public void run() {
                readAudioDataAndWrite2File();
            }
        });

    }

    /**
     * 在控制线程中,初始化AudioTrack
     */
    protected abstract AudioRecord initAudioRecord();

    private void readAudioDataAndWrite2File() {
        //文件写入完毕后,释放信号量即可
        readAudioDataAndWrite2FileImpl(mFilePath, mAudioRecord);
        mFileWriteSemaphore.release();
    }

    /**
     * 执行真正的从AudioRecord中读取数据和写入到文件中(此线程在工作线程中,不会阻塞控制线程)
     * 1. 需要通知开始真正的写入文件
     * 3. 有异常,通知异常
     * @param filePath
     * @param audioRecord
     */
    protected abstract void readAudioDataAndWrite2FileImpl(String filePath, AudioRecord audioRecord);

    @Override
    public void stopRecord() {
        logInfo("stopRecord");
        if(!isRecording()){
            logInfo("stopRecord not Recording ignore");
            return;
        }
        logInfo("stopRecord before execute stopRecordInner()");
        RecordUtils.getRecordCmdTask().start(new Runnable() {
            @Override
            public void run() {
                stopRecordInner();
            }
        });
    }

    private void stopRecordInner() {
        /**
         * - 不再录制中,直接返回
         - 停止将数据写入到文件中,强制等待结束,要不然可能会有状态的问题
         - 停止后,释放资源
         - 检测是否需要向外通知停止了录制
         */
        boolean realReleased = releaseRecordInner();//由于stopRecord 可能会被外面多次调用,并且并且异步操作,因而需要做校验,是否是真正的释放了,这样才能向外通知
        if(realReleased){
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
            logInfo("hasError no notify stop");
            return;
        }else{
            logInfo("noError notify stop");
        }
        notifyOnStopRecord(mFilePath);
    }

    /**
     * 是否真正的释放了录音器
     * @return
     */
    private boolean releaseRecordInner() {
        if(isAudioRecordRecording()){
            logInfo("isRecording , execute stop");
            try{
                mAudioRecord.stop();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            try{
                mAudioRecord.release();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            try{
                //等待写文件结束
                mFileWriteSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            releaseResource();
            return true;
        }else{
            logInfo("notRecording ,ignore stop");
        }
        return false;
    }

    /**
     * 在控制线程中释放资源
     */
    protected abstract void releaseResource();

    private boolean isAudioRecordRecording(){
        return mAudioRecord != null && mAudioRecord.getState() == AudioTrack.STATE_INITIALIZED && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    @Override
    public boolean isRecording() {
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
        return dir + File.separator + "record-" + System.currentTimeMillis() + "." + getFileSuffix();
    }

    protected abstract String getFileSuffix();

    protected void logInfo(String info){
        if(!LOG_ENABLE){
            return;
        }
        if(TextUtils.isEmpty(info)){
            return;
        }
        LogUtils.e(TAG, "[AudioRecordWrapper] "+info);
    }

    protected void setHasError(boolean hasError){
        mHasError = hasError;
    }
}
