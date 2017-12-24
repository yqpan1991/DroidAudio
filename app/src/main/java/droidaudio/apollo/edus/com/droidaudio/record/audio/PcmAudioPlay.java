package droidaudio.apollo.edus.com.droidaudio.record.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import droidaudio.apollo.edus.com.droidaudio.file.FileUtils;
import droidaudio.apollo.edus.com.droidaudio.file.IOUtils;

/**
 * AudioTrack pcm的播放
 * 仅支持使用一次,使用完成后,不可重复利用
 * Created by panda on 2017/12/22.
 */

public class PcmAudioPlay extends BasePlay {

    private final String TAG = "[PcmAudioPlay]";
    private final String LOG_TAG = "PcmAudioPlay";
    private final boolean LOG_ENABLE = true;

    private int AUDIO_SOURCE = AudioManager.STREAM_MUSIC;
    private int SAMPLE_RATE = 16000;
    private int CHANNEL_OUT_COUNT = AudioFormat.CHANNEL_OUT_STEREO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private String mFilePath;
    private boolean mHasError;
    private boolean mPlayCompleted;

    private AudioTrack mAudioTrack;
    private int mPlayBufferSize;
    private Semaphore mFileReadSemaphore;
    private AtomicInteger mState;
    private BufferedInputStream mBis = null;

    private long mFileLength;
    private long mCurrentReadLength;


    public PcmAudioPlay(){
        mFileReadSemaphore = new Semaphore(0);
        mState = new AtomicInteger(IPlay.IDLE);
        mFileLength = 0;
        mCurrentReadLength = 0;
    }

    @Override
    public void play(String url) {
        play(url, 0);
    }

    @Override
    public void play(final String url, final int pos) {
        //控制线程执行
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                playInner(url, pos);
            }
        });
    }

    private void playInner(String url, int pos) {
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
            //todo: 错误码未定,文件不存在
            stopInner();
            notifyOnError(mFilePath, 0, 0);
            return;
        }
        mState.set(IPlay.PREPARING);
        notifyOnPreparing(mFilePath);
        mPlayBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT);
        mAudioTrack = new AudioTrack(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT, mPlayBufferSize, AudioTrack.MODE_STREAM);
        mAudioTrack.setPlaybackPositionUpdateListener(mPlayBackPosUpdateListener);
        mAudioTrack.play();

        try {
            File file = new File(mFilePath);
            mBis = new BufferedInputStream(new FileInputStream(file));
            mFileLength = file.length();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log("file not found, path:"+ mFilePath);
            mHasError = true;
            stopInner();
            //todo: 异常code
            notifyOnError(mFilePath, 0, 0);
            return;
        }
        if(mFileLength <= 0){
            mHasError = true;
            stopInner();
            //todo: 异常code
            notifyOnError(mFilePath, 0, 0 );
            return;
        }
        mState.set(IPlay.PREPARED);
        notifyOnPrepared(mFilePath);
        PlayUtils.getPlayTask().start(new Runnable() {
            @Override
            public void run() {
                writeFileToAudioTrack();
            }
        });
        //准备播放的数据
        //启动异步线程,开始播放
        //先通知准备中
        //准备好了后,通知onPrepared
        //真正写入数据时,通知开始播放

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

    private void writeFileToAudioTrack() {
        byte[] writeBytes = new byte[mPlayBufferSize];
        int readLength = 0;
        //记录读取的长度,如果读取的长度和最终写入的长度相同,表示文件正常结束
        File file = new File(mFilePath);
        long currentKnownFileLength = file.length();
        try {
            boolean isFirstRead = false;
            while(isAudioTrackPlaying()){
                if((readLength = mBis.read(writeBytes, 0, writeBytes.length)) > 0){
                    if(!isFirstRead){
                        isFirstRead = true;
                        mState.set(IPlay.RUNNING);
                        notifyOnPlay(mFilePath);
                    }
                    mCurrentReadLength += readLength;
                    mAudioTrack.write(writeBytes, 0, readLength);
                }else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            mHasError = true;
            stopInner();
            //todo: 异常code
            notifyOnError(mFilePath, 0, 0);
        }  finally {
            // 在stop时正式关闭文件,否则,需要记录更多内容关于位置的信息的
        }
        //文件流在终止的地方关闭即可
        log("mCurrentReadLength:"+mCurrentReadLength+",fileLength:"+mFileLength+",currentKnownFileLength:"+currentKnownFileLength);
        if(mCurrentReadLength >= mFileLength){//读取结束,这里需要记录为>=,在华为P10上偶尔有个现象,很奇怪,Pcm录制时,记录的是文件关闭后,才会通知结束,但是文件并没有及时关闭,到时fileSize会大于文件的大小
            if(mFileLength != currentKnownFileLength){//有可能文件在读取的过程中,被别人篡改掉,此时便通知为错误即可
                log("file Size not match, but larger than initLength, close it normally");
            }
            //通过通知的方式,获取结果
            log("waiting for close");
            mAudioTrack.setNotificationMarkerPosition(1);
        }else{//非正常结束文件的播放,说明是外面操作导致的,直接释放信号量,等待外面的继续操作
            log("writeFileToAudioTrack intercept by other operation");
            if(mFileLength != currentKnownFileLength){//有可能文件在读取的过程中,被别人篡改掉,此时便通知为错误即可
                log("file Size not match for intercept, smaller than initLength, consider as error");
                mHasError = true;
                stop();
                notifyOnError(mFilePath, 0 , 0);
            }
        }
        mFileReadSemaphore.release();
    }

    @Override
    public void pause() {
        //启动控制线程,暂停播放
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
            try{
                mAudioTrack.pause();
            }catch (Exception ex){
                ex.printStackTrace();
                log("pauseInner pauseAudioTrack exception:"+ex.toString());
                mHasError = true;
                stopInner();
                //todo: 错误提示信息
                notifyOnError(mFilePath, 0, 0);
            }
            try {
                mFileReadSemaphore.acquire();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                log("pauseInner acquire permit exception:"+ex.toString());
                mHasError = true;
                stopInner();
                //todo: 错误提示信息
                notifyOnError(mFilePath, 0, 0);
            }
            if(!mHasError && isAudioTrackPaused()){
                notifyOnPause(mFilePath);
            }
        }
    }

    @Override
    public void stop() {
        //启动控制线程,停止播放
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                stopInner();
            }
        });

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
        IOUtils.closeSilently(mBis);
    }

    private void checkNotifyResult() {
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
        //1. 初始化了后,如果在播放中,对于播放中和暂停中,都flush,
        // 播放中,需要等待结果,暂停中,就不需要等待了,因为没有播放的数据,最终释放player即可, return true;
        if(isAudioTrackValid()){
            log("releaseAudioTrack valid");
            if(isAudioTrackPlaying()){
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
            }else if(isAudioTrackPaused()){
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
                log("audioTrack.getstate is stopped? "+(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED));
                //无效状态
            }
        }else{
            log("releaseAudioTrack not valid");
        }
        return false;
    }

    @Override
    public void resume() {
        //启动异步线程,继续播放
        PlayUtils.getPlayCmdTask().start(new Runnable() {
            @Override
            public void run() {
                resumeInner();
            }
        });
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

    /**
     * 不支持的播放,pcm如果要做seekTo,可以做三方库的引入,这里做封装即可
     * @param targetPosition
     */
    @Override
    public void seekTo(int targetPosition) {

    }

    @Override
    public String getPlayPath() {
        return mFilePath;
    }

    /**
     * 不支持获取pcm的长度
     * @return
     */
    @Override
    public int getDuration() {
        return 0;
    }

    /**
     * 不支持获取pcm的当前播放位置
     * @return
     */
    @Override
    public int getCurrentPosition() {
        return 0;
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

    @Override
    protected void notifyOnError(String filePath, int what, int extra) {
        //todo: 内部停止播放
        super.notifyOnError(filePath, what, extra);
    }

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
