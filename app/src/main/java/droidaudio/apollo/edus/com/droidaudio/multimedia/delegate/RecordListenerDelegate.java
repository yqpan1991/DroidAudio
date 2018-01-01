package droidaudio.apollo.edus.com.droidaudio.multimedia.delegate;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecordListener;

/**
 * Created by panda on 2018/1/1.
 */

public class RecordListenerDelegate implements IRecordListener {

    private Set<IRecordListener> mIRecordListenerSet;

    public RecordListenerDelegate() {
        mIRecordListenerSet = new CopyOnWriteArraySet<>();
    }

    public void addRecordListener(IRecordListener recordListener) {
        if (recordListener != null) {
            mIRecordListenerSet.add(recordListener);
        }
    }

    public void removeRecordListener(IRecordListener recordListener) {
        if (recordListener != null) {
            mIRecordListenerSet.remove(recordListener);
        }
    }

    @Override
    public void onStartRecord(String filePath) {
        Iterator<IRecordListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IRecordListener next = iterator.next();
            if (next != null) {
                next.onStartRecord(filePath);
            }
        }
    }

    @Override
    public void onStopRecord(String filePath) {
        Iterator<IRecordListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IRecordListener next = iterator.next();
            if (next != null) {
                next.onStopRecord(filePath);
            }
        }
    }

    @Override
    public void onRecordException(String filePath, int errorCode, String errorMsg) {
        Iterator<IRecordListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IRecordListener next = iterator.next();
            if (next != null) {
                next.onRecordException(filePath, errorCode, errorMsg);
            }
        }
    }
}
