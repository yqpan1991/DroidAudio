package droidaudio.apollo.edus.com.droidaudio.record.audio;

/**
 * Created by panda on 2017/12/17.
 */

public interface IRecord {

    void startRecord();
    void stopRecord();
    boolean isRecording();

    void addRecordListener(IRecordListener recordListener);
    void removeRecordListener(IRecordListener recordListener);
}
