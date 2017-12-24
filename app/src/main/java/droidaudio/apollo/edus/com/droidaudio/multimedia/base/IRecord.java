package droidaudio.apollo.edus.com.droidaudio.multimedia.base;

/**
 * Created by panda on 2017/12/17.
 */

public interface IRecord {

    void startRecord();
    void stopRecord();
    boolean isRecording();

    /**
     * 录音开始后,才能获取到录音的路径
     * @return
     */
    String getRecordPath();

    void addRecordListener(IRecordListener recordListener);
    void removeRecordListener(IRecordListener recordListener);
}
