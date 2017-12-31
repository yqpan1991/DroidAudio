package droidaudio.apollo.edus.com.droidaudio.multimedia.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.edus.apollo.opuslib.OpusTool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 * Created by panda on 2017/12/24.
 */

public class OpusAudioRecord extends AudioRecordWrapper {

    /**
     * 此参数请勿调整,由于opusTool的c层的参数决定
     */
    private final int WRITE_OPUS_FILE_BUFFER_SIZE = 1920;
    private final int AUDIO_RECORD_READ_BUFFER_MIN_SIZE = 4096;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRateInHz = 16000;
    private int mChannelCount = AudioFormat.CHANNEL_IN_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int recordBufferSize;
    private OpusTool mOpusTool;


    public OpusAudioRecord(Context context) {
        super(context);
    }

    @Override
    protected AudioRecord initAudioRecord() {
        mOpusTool = new OpusTool();
        recordBufferSize = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelCount, mAudioFormat);
        if (recordBufferSize <= 0) {
            recordBufferSize = 1280;
        }
        AudioRecord audioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelCount, mAudioFormat, recordBufferSize * 10);
        return audioRecord;
    }

    @Override
    protected void readAudioDataAndWrite2FileImpl(String filePath, AudioRecord audioRecord) {
//        1. 打开一个文件,打开后,真正开始录制后,再通知开始录制
//        2. 循环录制即可
//        3. 录音结束后,需要关闭文件
        if (!OpusTool.isLoadSucceed()) {
            notifyOnRecordException(filePath, IRecordListener.ERROR_RECORD_TYPE_NOT_SUPPORTED, "libopustool load failed");
            return;
        }

        boolean recordRet = mOpusTool.startOpusRecord(filePath);
        if (!recordRet) {
            notifyOnRecordException(filePath, IRecordListener.ERROR_FILE_WRITE_EXCEPTION, "startOpusRecord failed");
            return;
        }
        notifyOnStartRecord(filePath);

        int readLength = 0;
        boolean readStarted = false;

        ByteBuffer writeFileBuffer = ByteBuffer.allocateDirect(WRITE_OPUS_FILE_BUFFER_SIZE);
        writeFileBuffer.order(ByteOrder.nativeOrder());
        writeFileBuffer.clear();

        int audioRecordBufferSize = Math.max(recordBufferSize, AUDIO_RECORD_READ_BUFFER_MIN_SIZE);
        ByteBuffer audioRecordBuffer = ByteBuffer.allocateDirect(audioRecordBufferSize);
        audioRecordBuffer.order(ByteOrder.nativeOrder());
        //1. 将byteBuffer设置为写入的模式
        audioRecordBuffer.clear();
        boolean writeFileSucceed = false;

        while ((readLength = audioRecord.read(audioRecordBuffer, audioRecordBuffer.limit())) > 0) {
            if (!readStarted) {
                readStarted = true;
            }
            //1. 将byteBuffer设置为读出来的模式 done
            audioRecordBuffer.limit(readLength);
            audioRecordBuffer.rewind();
            //2. 将数据写入到一个新的buffer
            //3. 写满后,开始写入到opus的文件
            writeFileSucceed = writeAudioBufferToFile(audioRecordBuffer, writeFileBuffer, false);
            //4. 再次设置为写入的模式即可 done
            audioRecordBuffer.clear();
            if(!writeFileSucceed){
                setHasError(true);
                notifyOnRecordException(filePath, IRecordListener.ERROR_FILE_WRITE_EXCEPTION, "write opus frame error");
                break;
            }
        }
        //再次判断是否还有剩余的数据,如果存在,将剩余的数据,同样写入到文件中
        if (!readStarted) {
            //文件没有来得及开始写,就结束了
        } else {//可能存在剩余的数据,将剩余的数据,同样写入
            if(!writeFileSucceed){
                writeAudioBufferToFile(audioRecordBuffer, writeFileBuffer, true);
            }
        }
        mOpusTool.stopOpusRecord();
    }

    private boolean writeAudioBufferToFile(ByteBuffer audioRecordBuffer, ByteBuffer writeFileBuffer, boolean forceFlush) {
        if (!forceFlush) {
            while (audioRecordBuffer.hasRemaining()) {
                int audioBufferOldLimit = -1;

                if (audioRecordBuffer.remaining() > writeFileBuffer.remaining()) {//读取的数据量大于可以写入的数据量
                    audioBufferOldLimit = audioRecordBuffer.limit();
                    audioRecordBuffer.limit(audioRecordBuffer.position() + writeFileBuffer.remaining());
                }

                writeFileBuffer.put(audioRecordBuffer);
                logInfo("writeFileBuffer limit:"+writeFileBuffer.limit()+",writeFileBuffer pos:"+writeFileBuffer.position());

                if (writeFileBuffer.limit() == writeFileBuffer.position()) {//数据写满了
                    writeFileBuffer.flip();
                    boolean succeed = mOpusTool.writeOpusFrame(writeFileBuffer, writeFileBuffer.limit());
                    if (!succeed) {
                        logInfo("write failed");
                        return false;
                    } else {
                        writeFileBuffer.clear();
                        logInfo("write succeed");
                    }
                }

                if (audioBufferOldLimit > 0) {
                    audioRecordBuffer.limit(audioBufferOldLimit);
                }
            }
        } else {
            if (writeFileBuffer.position() > 0) {//有剩余未写入到文件的数据
                writeFileBuffer.flip();
                mOpusTool.writeOpusFrame(writeFileBuffer, writeFileBuffer.limit());
                writeFileBuffer.rewind();
            }
        }

        return true;
    }

    @Override
    protected void releaseResource() {

    }

    @Override
    protected String getFileSuffix() {
        return "ogg";
    }
}
