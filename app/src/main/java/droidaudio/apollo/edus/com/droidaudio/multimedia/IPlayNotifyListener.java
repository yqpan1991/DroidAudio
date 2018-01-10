package droidaudio.apollo.edus.com.droidaudio.multimedia;

import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;

/**
 * Created by panda on 2018/1/2.
 */

public interface IPlayNotifyListener extends IPlayerListener {
    void notifyOnProgressChanged(String filePath, long progress, long duration);
}
