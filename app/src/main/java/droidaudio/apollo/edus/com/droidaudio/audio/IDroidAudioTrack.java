package droidaudio.apollo.edus.com.droidaudio.audio;

import android.media.MediaPlayer;

/**
 * Created by PandaPan on 2016/11/13.
 * 由于android自身媒体库的限制,因而暂时没有添加seek的方法,在添加第三方的解析库之后,就可以做这些操作
 */

public interface IDroidAudioTrack {
    int PLAY_STATE_START = 1;
    int PLAY_STATE_PAUSE = 2;
    int PLAY_STATE_RESUME = 3;
    int PLAY_STATE_STOP = 4;

    int MEDIA_ERROR_UNKNOWN = 0;
    int MEDIA_ERROR_INVALID_PATH = 1;
    int MEDIA_ERROR_IO = 1000;


    //播放
    //暂停
    //继续播放
    //停止
    //seekto,需要有相应的解码器才行,因而需要记录当前文件的读取位置
    //获取当前播放进度
    //获取总时长
    //设置播放的监听
    //需要存在控制的信息,因而要有控制的线程,还要有不断读取数据的线程,整体的控制参数在AudioTrack
    void play(String filePath);
    void pause();
    void resume();
    void stop();
    boolean isPlaying();
    String getFilePath();

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    void setOnPreparedListener(OnPreparedListener listener);

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    interface OnPreparedListener
    {
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the mp that is ready for playback
         */
        void onPrepared(IDroidAudioTrack mp);
    }

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    interface OnCompletionListener
    {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param mp the MediaPlayer that reached the end of the file
         */
        void onCompletion(MediaPlayer mp);
    }


    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    void setOnCompletionListener(OnCompletionListener listener);


    interface OnPlayStateChangedListener{
        void onPlayStateChanged(int playState);
    }

    void setOnPlayStateChangedListener(OnPlayStateChangedListener listener);

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    interface OnErrorListener
    {
        /**
         * Called to indicate an error.
         *
         * @param mp      the MediaPlayer the error pertains to
         * @param what    the type of error that has occurred:
         * <ul>
         * <li>{@link #MEDIA_ERROR_UNKNOWN}
         * </ul>
         * @param extra an extra code, specific to the error. Typically
         * implementation dependent.
         * <ul>
         * <li>{@link #MEDIA_ERROR_IO}
         * <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
         * </ul>
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        boolean onError(IDroidAudioTrack mp, int what, int extra);
    }

    void setOnErrorListener(OnErrorListener listener);



}
