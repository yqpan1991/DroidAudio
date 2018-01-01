package droidaudio.apollo.edus.com.droidaudio.multimedia.delegate;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;

/**
 * Created by panda on 2018/1/1.
 */

public class PlayerListenerDelegate implements IPlayerListener {

    private Set<IPlayerListener> mIRecordListenerSet;

    public PlayerListenerDelegate() {
        mIRecordListenerSet = new CopyOnWriteArraySet<>();
    }

    public void addIPlayerListener(IPlayerListener listener) {
        if (listener != null) {
            mIRecordListenerSet.add(listener);
        }
    }

    public void removeIPlayerListener(IPlayerListener listener) {
        if (listener != null) {
            mIRecordListenerSet.remove(listener);
        }
    }


    @Override
    public void onPreparing(String filePath) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onPreparing(filePath);
            }
        }
    }

    @Override
    public void onPrepared(String filePath) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onPrepared(filePath);
            }
        }
    }

    @Override
    public void onPause(String filePath) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onPause(filePath);
            }
        }
    }

    @Override
    public void onPlay(String filePath) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onPlay(filePath);
            }
        }
    }

    @Override
    public void onError(String filePath, int what, int extra) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onError(filePath, what, extra);
            }
        }
    }

    @Override
    public void onStopped(String filePath) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onStopped(filePath);
            }
        }
    }

    @Override
    public void onComplete(String filePath) {
        Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
        while (iterator.hasNext()) {
            IPlayerListener next = iterator.next();
            if (next != null) {
                next.onComplete(filePath);
            }
        }
    }
}
