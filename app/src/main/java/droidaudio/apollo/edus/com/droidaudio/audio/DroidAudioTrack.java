package droidaudio.apollo.edus.com.droidaudio.audio;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import droidaudio.apollo.edus.com.droidaudio.file.FileUtils;
import droidaudio.apollo.edus.com.droidaudio.file.IOUtils;
import droidaudio.apollo.edus.com.droidaudio.thread.SingleExecutor;

/**
 * Created by PandaPan on 2016/11/13.
 */

public class DroidAudioTrack implements IDroidAudioTrack {
    private int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private int SAMPLE_RATE = 16000;
    private int CHANNEL_IN_COUNT = AudioFormat.CHANNEL_IN_STEREO;
    private int CHANNEL_OUT_COUNT = AudioFormat.CHANNEL_OUT_STEREO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private OnCompletionListener mOnCompletionListener;
    private OnPlayStateChangedListener mOnPlayStateChangedListener;
    private OnPreparedListener mOnPreparedListener;
    private OnErrorListener mOnErrorListener;

    private AudioTrack mAudioTrack;
    private SingleExecutor mExecutor;
    private Thread mPlayThread;
    private String mFilePath;
    private Handler mUIHandler;
    private int mPlayBufferSize;
    private BufferedInputStream mInputStream;

    public DroidAudioTrack() {
        mExecutor = new SingleExecutor("DroidAudioTrack");
        mUIHandler = new Handler();
    }

    @Override
    public void play(final String filePath) {
        mExecutor.postRunnable(new Runnable() {
            @Override
            public void run() {
                playInner(filePath);
            }
        });
    }

    private void playInner(String filePath) {
        //1. 文件存在,并且是处于有效的状态
        mFilePath = filePath;
        if (FileUtils.isFileExists(filePath)) {
            if(!isAudioTrackValid()){
                mPlayBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT);
                mAudioTrack = new AudioTrack(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT, mPlayBufferSize, AudioTrack.MODE_STREAM);
                mAudioTrack.play();
                if(mPlayThread == null){
                    try {
                        mInputStream = new BufferedInputStream(new FileInputStream(mFilePath));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        postError(MEDIA_ERROR_INVALID_PATH, MEDIA_ERROR_IO);
                        stop();
                        return;
                    }
                    mPlayThread = new Thread(makeWriteDataToAudioTrackRunnable());
                    mPlayThread.start();
                }
                if(mOnPreparedListener != null){
                    mOnPreparedListener.onPrepared(this);
                }
                if(mOnPlayStateChangedListener != null){
                    mOnPlayStateChangedListener.onPlayStateChanged(PLAY_STATE_START);
                }
            }
        } else {
            postError(MEDIA_ERROR_INVALID_PATH, MEDIA_ERROR_IO);
        }
    }

    @Override
    public void pause() {
        mExecutor.postRunnable(new Runnable() {
            @Override
            public void run() {
                pauseInner();
            }
        });
    }

    private void pauseInner() {
        if(isAudioTrackValid()){
            switch (mAudioTrack.getPlayState()){
                case AudioTrack.PLAYSTATE_PAUSED:
                    break;
                case AudioTrack.PLAYSTATE_PLAYING:
                    mAudioTrack.pause();
                    mPlayThread = null;
                    postStateChanged(PLAY_STATE_PAUSE);
                    break;
                case AudioTrack.PLAYSTATE_STOPPED:
                    break;
            }
        }
    }

    @Override
    public void resume() {
        mExecutor.postRunnable(new Runnable() {
            @Override
            public void run() {
                resumeInner();
            }
        });
    }

    private void resumeInner() {
        if(isAudioTrackValid()){
            switch (mAudioTrack.getPlayState()){
                case AudioTrack.PLAYSTATE_PAUSED:
                    mAudioTrack.play();
                    if(mPlayThread == null){
                        mPlayThread = new Thread(makeWriteDataToAudioTrackRunnable());
                        mPlayThread.start();
                    }
                    postStateChanged(PLAY_STATE_RESUME);
                    break;
                case AudioTrack.PLAYSTATE_PLAYING:
                    break;
                case AudioTrack.PLAYSTATE_STOPPED:
                    break;
            }
        }
    }

    @Override
    public void stop() {
        mExecutor.postRunnable(new Runnable() {
            @Override
            public void run() {
                stopInner();
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return isAudioTrackValid() && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }

    private void stopInner() {
        if(isAudioTrackValid()){
            switch (mAudioTrack.getPlayState()){
                case AudioTrack.PLAYSTATE_PAUSED:
                    stopImpl();
                    break;
                case AudioTrack.PLAYSTATE_PLAYING:
                    pauseInner();
                    stopImpl();
                    break;
                case AudioTrack.PLAYSTATE_STOPPED:
                    //do nothing
                    break;
            }
        }
        mExecutor.shutDown();
    }

    private void stopImpl(){
        mAudioTrack.flush();
        mAudioTrack.stop();
        synchronized (this){
            IOUtils.closeSilently(mInputStream);
            mInputStream = null;
        }
        postStateChanged(PLAY_STATE_STOP);
    }

    @Override
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public void setOnPlayStateChangedListener(OnPlayStateChangedListener listener) {
        mOnPlayStateChangedListener = listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private boolean isAudioTrackValid(){
        return mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED;
    }

    private void postError(final int errorCode, final int extra) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnErrorListener != null) {
                    mOnErrorListener.onError(DroidAudioTrack.this, errorCode, extra);
                }
            }
        });
    }

    private Runnable makeWriteDataToAudioTrackRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                writeDataToAudioTrack();
            }
        };
    }

    private void writeDataToAudioTrack() {
        try {
            byte[] writeBytes = new byte[mPlayBufferSize];
            while(isAudioTrackValid() && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                int read = 0;
                synchronized (DroidAudioTrack.this){
                    if(mInputStream != null){
                        read = mInputStream.read(writeBytes, 0, mPlayBufferSize);
                    }
                }
                if(read > 0){
                    mAudioTrack.write(writeBytes, 0, read);
                }else{
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            stop();
                        }
                    });
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            postError(MEDIA_ERROR_INVALID_PATH, MEDIA_ERROR_IO);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            });
        }
    }

    private void postStateChanged(final int state){
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mOnPlayStateChangedListener != null){
                    mOnPlayStateChangedListener.onPlayStateChanged(state);
                }

            }
        });
    }
}
