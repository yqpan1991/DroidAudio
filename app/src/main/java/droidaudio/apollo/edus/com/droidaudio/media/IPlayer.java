package droidaudio.apollo.edus.com.droidaudio.media;

/**
 * Created by PandaPan on 2017/2/6.
 * 一个player,仅用于播放一个媒体文件,虽然可以兼容播放多个文件,不建议这样使用,简单,不容易出现问题
 */

public interface IPlayer {

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
    void start(String url);

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
}
