package droidaudio.apollo.edus.com.droidaudio.multimedia.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import droidaudio.apollo.edus.com.droidaudio.file.IOUtils;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 * pcm录制的封装类<br/>
 * 所有向外暴露的方法,都需要在ui线程调用
 * Created by panda on 2017/12/20.
 */

public class PcmAudioRecord extends AudioRecordWrapper {

    private final String TAG = this.getClass().getSimpleName();

    private int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private int SAMPLE_RATE = 16000;
    private int CHANNEL_IN_COUNT = AudioFormat.CHANNEL_IN_STEREO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private int mRecordBufferSize;

    public PcmAudioRecord(Context context){
        super(context);
    }

    @Override
    protected AudioRecord initAudioRecord() {
        mRecordBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_COUNT, AUDIO_FORMAT);
        return new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_IN_COUNT, AUDIO_FORMAT, mRecordBufferSize);
    }


    @Override
    protected void readAudioDataAndWrite2FileImpl(String filePath, AudioRecord audioRecord) {
        //1. 打开文件,文件打开失败,直接通知异常
        //2. 创建变量,开始循环读取数据,将读取的数据,都写入到文件中,只需要判断read的结果即可,不需要再判断其他的状态
        //3. 在状态无效时,向外通知结果即可,录音结束,如果有异常,向外通知异常
        BufferedOutputStream bos = null;
        boolean realStarted = false;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
            logInfo("before notify startRecord()");
            notifyOnStartRecord(filePath);
            byte[] bufferBytes = new byte[mRecordBufferSize];
            int readLength = -1;
            while((readLength = audioRecord.read(bufferBytes, 0, bufferBytes.length)) > 0){
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
            setHasError(true);
            notifyOnRecordException(filePath, IRecordListener.ERROR_FILE_WRITE_EXCEPTION, "writeFileNotFound:"+e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logInfo("readAudioDataAndWrite2File readAudioDataAndWrite2File exception:"+e.toString());
            setHasError(true);
            notifyOnRecordException(filePath, IRecordListener.ERROR_FILE_WRITE_EXCEPTION, "writeFileError:"+e.toString());
        } finally {
            IOUtils.closeSilently(bos);
        }
    }

    protected void releaseResource() {
        //释放资源
    }

}
