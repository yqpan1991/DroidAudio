package droidaudio.apollo.edus.com.droidaudio.media;

/**
 * Created by PandaPan on 2017/2/6.
 */

public interface IRecorderListener {
    int STOP_REASON_OUTTER_STOP = 0;
    int STOP_REASON_INNER_START = 1;
    int STOP_REASON_INNER_RECORDING = 2;
    int STOP_REASON_OUTTER_NO_FILE = 3;
    int STOP_REASON_WHAT_NORMAL = 0;
    int STOP_REASON_EXTRA_NORMAL = 0;

    void onStart(String filePath);
    void onStop(String filePath, long duration);
    void onError(int errorReason, int what, int extra);
}
