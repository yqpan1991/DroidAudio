package droidaudio.apollo.edus.com.droidaudio.media;

/**
 * Created by PandaPan on 2017/2/6.
 */

public interface IPlayerListener {
    void onPreparing(String filePath);
    void onPrepared(String filePath);
    void onStart(String filePath);
    void onPause(String filePath);
    void onResume(String filePath);
    void onError(String filePath, int what, int extra);
    void onStopped(String filePath);
    void onComplete(String filePath);
    void onProgressChanged(String filePath, int curPosition, int duration);
}
