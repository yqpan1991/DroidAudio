package droidaudio.apollo.edus.com.droidaudio.record.audio;

import android.os.Handler;
import android.os.Looper;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import droidaudio.apollo.edus.com.droidaudio.Utils.MainLooper;
import droidaudio.apollo.edus.com.droidaudio.media.IPlayer;
import droidaudio.apollo.edus.com.droidaudio.media.IPlayerListener;

/**
 * Created by PandaPan on 2017/2/7.
 */

public abstract class BasePlay implements IPlay {

    private Set<IPlayerListener> mIRecordListenerSet = new CopyOnWriteArraySet<>();

    public BasePlay(){

    }

    @Override
    public void addPlayListener(IPlayerListener listener) {
        if(listener != null){
            mIRecordListenerSet.add(listener);
        }
    }

    @Override
    public void removePlayListener(IPlayerListener listener) {
        if(listener != null){
            mIRecordListenerSet.remove(listener);
        }
    }

    protected void notifyOnPrepared(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onPrepared(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnPreparing(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onPreparing(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnPlay(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onPlay(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnPause(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onPause(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnError(final String filePath, final int what, final int extra){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onError(filePath, what, extra);
                    }
                }
            }
        });
    }

    protected void notifyOnStopped(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onStopped(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnComplete(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IPlayerListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IPlayerListener next = iterator.next();
                    if(next != null){
                        next.onComplete(filePath);
                    }
                }
            }
        });
    }


}
