package droidaudio.apollo.edus.com.droidaudio.multimedia.base;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import com.edus.apollo.opuslib.OpusTool;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import droidaudio.apollo.edus.com.droidaudio.file.FileUtils;
import droidaudio.apollo.edus.com.droidaudio.utils.MainLooper;

/**
 * Created by panda on 2017/12/31.
 */

public class OpusAudioTrack extends BasePlay {

    private final String TAG = "[OpusAudioTrack]";
    private final String LOG_TAG = "OpusAudioTrack";
    private final boolean LOG_ENABLE = true;

    private final int AUDIO_SOURCE = AudioManager.STREAM_MUSIC;
    private final int SAMPLE_RATE = 48000;
    private final int CHANNEL_OUT_COUNT = AudioFormat.CHANNEL_OUT_MONO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int AUDIO_PLAY_BUFFER_MIN_SIZE = 3840;

    private String mFilePath;
    private boolean mHasError;
    private boolean mPlayCompleted;

    private AudioTrack mAudioTrack;
    private int mPlayBufferSize;
    private Semaphore mFileReadSemaphore;
    private AtomicInteger mState;
    private OpusTool mOpusTool;
    private volatile long mDecodePos;
    private boolean mDecodeFinished;

    public OpusAudioTrack(){
        mFileReadSemaphore = new Semaphore(0);
        mState = new AtomicInteger(IPlay.IDLE);
        mDecodeFinished = false;
    }

    @Override
    public void play(String url) {
        play(url, 0);
    }

    @Override
    public void play(final String url, final int pos) {
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                playInner(url, pos);
            }
        });
    }

    private void playInner(String url, int pos) {
        if(!OpusTool.isLoadSucceed()){
            mHasError = true;
            stopInner();
            notifyOnError(mFilePath, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_UNSUPPORTED);
            return;
        }
        if(TextUtils.isEmpty(url)){
            return;
        }
        if(TextUtils.isEmpty(mFilePath)){
            mFilePath = url;
        }else{
            //ignore
            return;
        }
        if(!FileUtils.isFileExists(mFilePath)){
            //设置hasError, 通知异常
            mHasError = true;
            stopInner();
            notifyOnError(mFilePath, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_IO);
            return;
        }
        mFilePath = (new File(mFilePath)).getAbsolutePath();
        mOpusTool = new OpusTool();
        if(!mOpusTool.isOpusFileFormat(mFilePath)){
            mHasError = true;
            stopInner();
            notifyOnError(mFilePath, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_UNSUPPORTED);
            return;
        }
        mState.set(IPlay.PREPARING);
        notifyOnPreparing(mFilePath);
        //找文件
        boolean openSucceed = mOpusTool.openFile(mFilePath);
        if(!openSucceed){
            mHasError = true;
            stopInner();
            notifyOnError(mFilePath, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_IO);
            return;
        }

        mPlayBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT);
        if (mPlayBufferSize <= 0) {
            mPlayBufferSize = AUDIO_PLAY_BUFFER_MIN_SIZE;
        }
        mAudioTrack = new AudioTrack(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT, mPlayBufferSize, AudioTrack.MODE_STREAM);
        mAudioTrack.setPlaybackPositionUpdateListener(mPlayBackPosUpdateListener);
        mState.set(IPlay.PREPARED);
        notifyOnPrepared(mFilePath);
        if(pos > 0){
            mAudioTrack.pause();
            seekToInner(pos);
        }else{
            mAudioTrack.play();
            PlayUtils.getPlayTask().start(new Runnable() {
                @Override
                public void run() {
                    writeFileToAudioTrack();
                }
            });
        }
    }

    private void writeFileToAudioTrack() {
        //如果当前在播放中,读取文件,并且写入到audioTrack
        ByteBuffer readOpusBuffer = ByteBuffer.allocateDirect(mPlayBufferSize);
        readOpusBuffer.order(ByteOrder.nativeOrder());
        int[] readOpusArgs = new int[3];
        int size = 0;
        boolean isFirstRead = true;
        boolean finished = false;//1 表示为finished
        mState.set(IPlay.RUNNING);
        notifyOnPlay(mFilePath);
        while(isAudioTrackPlaying()){
            mOpusTool.readFile(readOpusBuffer, mPlayBufferSize, readOpusArgs);
            size = readOpusArgs[0];
            mDecodePos = readOpusArgs[1];
            finished = readOpusArgs[2] == OpusTool.READ_OPUS_FINISHED;
            if(size > 0){//表示有读取的数据
                if(isFirstRead){
                    isFirstRead =false;
                }
                //写入到audioTrack中
                readOpusBuffer.limit(size);
                readOpusBuffer.rewind();
                mAudioTrack.write(readOpusBuffer.array(), 0, size);
                readOpusBuffer.clear();
            }
            if(finished){
                //通知要结束
                mDecodeFinished = true;
                mAudioTrack.setNotificationMarkerPosition(1);
                break;
            }
        }
        mFileReadSemaphore.release();
    }

    @Override
    public void pause() {
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                pauseInner();
            }
        });
    }

    private void pauseInner() {
        //1. 设置为暂停即可
        if(isAudioTrackPlaying()){
            int errorCode = 0;
            try{
                mAudioTrack.pause();
            }catch (Exception ex){
                ex.printStackTrace();
                log("pauseInner pauseAudioTrack exception:"+ex.toString());
                mHasError = true;
                errorCode = IPlay.MEDIA_ERROR_ILLEGAL_STATE;
            }
            try {
                mFileReadSemaphore.acquire();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                log("pauseInner acquire permit exception:"+ex.toString());
            }
            if(!mHasError && isAudioTrackPaused()){
                notifyOnPause(mFilePath);
            }else if(mHasError){
                stopInner();
                notifyOnError(mFilePath, errorCode, 0);
            }
        }
    }

    @Override
    public void stop() {
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                stopInner();
            }
        });
    }

    @Override
    public void resume() {
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                resumeInner();
            }
        });
    }

    @Override
    public boolean isSupportSeekTo() {
        return true;
    }

    private void resumeInner() {
        //当前在暂停中
        //1. 新创建读取的线程
        //2. 从指定的位置继续开始往下读
        if(!isAudioTrackPaused()){
            return;
        }
        mAudioTrack.play();
        PlayUtils.getPlayTask().start(new Runnable() {
            @Override
            public void run() {
                writeFileToAudioTrack();
            }
        });
    }

    @Override
    public void seekTo(final int targetPosition) {
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                seekToInner(targetPosition);
            }
        });
    }

    private void seekToInner(int targetPosition) {
        if(isAudioTrackValid()){
            switch (mAudioTrack.getPlayState()){
                case AudioTrack.PLAYSTATE_PAUSED:
                    //seekTo opus的文件
                    seekOpusFile(targetPosition);
                    resumeInner();
                    break;
                case AudioTrack.PLAYSTATE_PLAYING:
                    //先暂停
                    pauseInner();
                    seekOpusFile(targetPosition);
                    resumeInner();
                    break;
            }

        }
    }

    private void seekOpusFile(int targetPosition) {
        //播放到文件的对应位置
        if(targetPosition <= 0){
            targetPosition = 0;
        }
        if(mDecodeFinished){
            mOpusTool.closeFile();
            mOpusTool.openFile(mFilePath);
            mDecodeFinished = false;
        }
        if(mOpusTool.getPcmDuration() <= 0){
            mHasError = true;
            //todo: 特殊的错误码
            notifyOnError(mFilePath, 0, 0);
            stopInner();
            return;
        }
        long pcmPosition = (long) (targetPosition * 48.0f);
        if(pcmPosition > mOpusTool.getPcmDuration()){
            pcmPosition = 0;
        }
        float progress = pcmPosition * 1.0f / mOpusTool.getPcmDuration();
        mOpusTool.seekFile(progress);
    }

    @Override
    public String getPlayPath() {
        return mFilePath;
    }

    @Override
    public int getDuration() {
        if(mOpusTool != null){
            //todo: 这里获取到的长度有误......
            return (int) mOpusTool.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return (int) OpusTool.convertPcm2NormalDuration(mDecodePos);
    }

    @Override
    public boolean isPlaying() {
        return mState.get() == IPlay.RUNNING && (isAudioTrackPlaying());
    }

    @Override
    public int getState() {
        return mState.get();
    }

    private boolean isAudioTrackPlaying(){
        return isAudioTrackValid() && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }

    private boolean isAudioTrackPaused(){
        return isAudioTrackValid() && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED;
    }

    private boolean isAudioTrackValid(){
        return mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED;
    }


    private void handlePlayCompleted() {
        log("handlePlayCompleted");
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                //正常播放结束
                handlePlayCompletedInner();
            }
        });

    }

    private void handlePlayCompletedInner() {
        log("handlePlayCompletedInner");
        mPlayCompleted = true;
        stopInner();
    }

    private void stopInner() {
        //异步线程开启的停止,
        //1. 停止数据的写入
        //2. 关闭AudioTrack
        //3. 检查是否有错误,有错误,不再通知
        //4. 没有错误,是否是正常完成,如果是,直接关闭,通知播放完成即可
        //5. 否则,通知播放停止
        //6. 关闭文件流
        log("stopInner()");
        boolean realRealeased = releaseAudioTrack();
        mState.set(IPlay.IDLE);
        if(realRealeased){
            checkNotifyResult();
        }
    }

    private void checkNotifyResult() {
        //1. 是否有错误
        //2. 是否完成了
        //3. 其他情况下,通知
        if(mHasError){
            log("checkNotifyResult has error no notify");
            return;
        }else if(mPlayCompleted){
            log("checkNotifyResult notifyOnComplete");
            notifyOnComplete(mFilePath);
        }else{
            log("checkNotifyResult notify stop");
            notifyOnStopped(mFilePath);
        }
    }

    private boolean releaseAudioTrack() {
        //如果是有效的释放了,返回true,否则,返回false
        if(isAudioTrackValid()){
            if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                mAudioTrack.flush();
                try{
                    mAudioTrack.stop();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                try{
                    mAudioTrack.release();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                try {
                    mFileReadSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log("releaseAudioTrack playing");
                return true;
            }else if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED){
                mAudioTrack.flush();
                try{
                    mAudioTrack.stop();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                try{
                    mAudioTrack.release();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                log("releaseAudioTrack paused");
                return true;
            }else{
                //illegalState
                log("audioTrack.getstate is stopped? "+(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED));
            }
        }else{
            log("releaseAudioTrack not valid");
        }
        return false;
    }


    private AudioTrack.OnPlaybackPositionUpdateListener mPlayBackPosUpdateListener = new AudioTrack.OnPlaybackPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioTrack track) {
            handlePlayCompleted();
        }

        @Override
        public void onPeriodicNotification(AudioTrack track) {

        }
    };

    private void log(String info){
        if(!LOG_ENABLE){
            return;
        }
        if(TextUtils.isEmpty(info)){
            return;
        }
        Log.e(LOG_TAG, TAG+" "+info);
    }
}
