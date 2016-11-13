package droidaudio.apollo.edus.com.droidaudio.audio;

/**
 * Created by PandaPan on 2016/11/13.
 */

public interface IDroidAudioRecorder {
    int ERROR_WHAT_START_RECORD = 1;
    int ERROR_WHAT_DURING_RECORD = 2;
    int ERROR_EXTRA_EMPTY_FILE_PATH = 1000;
    int ERROR_EXTRA_WRITE_FILE_ERROR = 1001;

    void startRecord();
    void stopRecord();
    boolean isRecoding();
    void setOnDroidAudioRecordListener(OnDroidAudioRecordListener listener);
    OnDroidAudioRecordListener getOnDroidAudioRecordListener();

    interface OnDroidAudioRecordListener{
        void onRecordStart(String filePath);
        void onRecordStop(String filePath);
        void onRecordError(IDroidAudioRecorder recorder, int what, int extra);
    }

}
