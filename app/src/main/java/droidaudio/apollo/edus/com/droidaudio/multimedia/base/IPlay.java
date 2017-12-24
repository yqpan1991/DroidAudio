package droidaudio.apollo.edus.com.droidaudio.multimedia.base;

/**
 * Created by panda on 2017/12/18.
 */

public interface IPlay {

    /**
     * operate media by wrong operation,caused IllegalStateException
     */
    public static final int MEDIA_ERROR_ILLEGAL_STATE = 10000;

    /**
     * mediaplayer is idle
     */
    int IDLE = 0;
    /**
     * mediaplayer is initialized
     */
    int INITIALIZED = 1;
    /**
     * mediaplayer is preparing
     */
    int PREPARING = 2;
    /**
     * mediaplayer is prepared
     */
    int PREPARED = 3;
    /**
     * mediaplayer is started or paused
     */
    int RUNNING = 4;
    //other state not stated above will just release mediaplayer,so needn't care

    /**
     * 播放某个音频
     * @param url 在线url或者本地的路径
     */
    void play(String url);

    /**
     * 播放某个音频,直接从对应的位置开始播放
     * @param url
     * @param pos
     */
    void play(String url, int pos);

    /**
     * 暂停播放,如果没有播放的音频文件,会回调播放错误
     */
    void pause();

    /**
     * 停止播放,会清空当前播放的信息,如果当前没有播放的文件,会回调播放错误
     */
    void stop();

    /**
     * 继续播放,如果没有播放的音频文件,会回调播放错误
     */
    void resume();

    /**
     * seekTo操作,如果当前没有播放的音频文件,会回调错误
     * @param targetPosition
     */
    void seekTo(int targetPosition);
    String getPlayPath();
    int getDuration();
    int getCurrentPosition();
    boolean isPlaying();
    int getState();
    void addPlayListener(IPlayerListener listener);
    void removePlayListener(IPlayerListener listener);

}
