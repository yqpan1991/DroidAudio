package droidaudio.apollo.edus.com.droidaudio.record.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.edus.apollo.common.utils.log.LogUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import droidaudio.apollo.edus.com.droidaudio.Utils.MainLooper;
import droidaudio.apollo.edus.com.droidaudio.file.IOUtils;

/**
 * pcm录制的封装类<br/>
 * 所有向外暴露的方法,都需要在ui线程调用
 * Created by panda on 2017/12/20.
 */

public class PcmRecordWrapper extends BaseRecord {

    private final String TAG = this.getClass().getSimpleName();

    private int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private int SAMPLE_RATE = 16000;
    private int CHANNEL_IN_COUNT = AudioFormat.CHANNEL_IN_STEREO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private Context mContext;
    private boolean mHasError;
    private AudioRecord mAudioRecord;
    private int mRecordBufferSize;
    private boolean mIsRecording;
    private String mFilePath;
    private Semaphore mFileWriteSemaphore;
    private boolean LOG_ENABLE = true;

    public PcmRecordWrapper(Context context){
        if (context == null) {
            throw new RuntimeException("MediaRecordWrapper context cannot be null");
        }
        mContext = context.getApplicationContext();
        //todo: 此处需要测试
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
        mRecordBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_COUNT, AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_IN_COUNT, AUDIO_FORMAT, mRecordBufferSize);
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

    private void readAudioDataAndWrite2File() {
        //1. 打开文件,文件打开失败,直接通知异常
        //2. 创建变量,开始循环读取数据,将读取的数据,都写入到文件中,只需要判断read的结果即可,不需要再判断其他的状态
        //3. 在状态无效时,向外通知结果即可,录音结束,如果有异常,向外通知异常
        BufferedOutputStream bos = null;
        boolean realStarted = false;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(mFilePath));
            logInfo("before notify startRecord()");
            notifyOnStartRecord(mFilePath);
            byte[] bufferBytes = new byte[mRecordBufferSize];
            int readLength = -1;
            while((readLength = mAudioRecord.read(bufferBytes, 0, bufferBytes.length)) > 0){
                if(realStarted == false){
                    logInfo("start write data to file");
                    realStarted = true;
                }
                bos.write(bufferBytes, 0, readLength);
            }
            logInfo("end write data to file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logInfo("readAudioDataAndWrite2File readAudioDataAndWrite2File exception:"+e.toString());
            mHasError = true;
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_FILE_WRITE_EXCEPTION, "writeFileNotFound:"+e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logInfo("readAudioDataAndWrite2File readAudioDataAndWrite2File exception:"+e.toString());
            mHasError = true;
            notifyOnRecordException(mFilePath, IRecordListener.ERROR_FILE_WRITE_EXCEPTION, "writeFileError:"+e.toString());
        } finally {
            IOUtils.closeSilently(bos);
        }
        mFileWriteSemaphore.release();
    }

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
            return true;
        }else{
            logInfo("notRecording ,ignore stop");
        }
        return false;
    }

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
        return dir + File.separator + "record-" + System.currentTimeMillis() + ".pcm";
    }

    private void logInfo(String info){
        if(!LOG_ENABLE){
            return;
        }
        if(TextUtils.isEmpty(info)){
           return;
        }
        LogUtils.e(TAG, "[PcmRecordWrapper] "+info);
    }
}
