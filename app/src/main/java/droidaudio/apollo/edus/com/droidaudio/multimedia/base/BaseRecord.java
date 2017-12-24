package droidaudio.apollo.edus.com.droidaudio.multimedia.base;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import droidaudio.apollo.edus.com.droidaudio.Utils.MainLooper;

/**
 * Created by panda on 2017/12/17.
 */

public abstract class BaseRecord implements IRecord {

    private Set<IRecordListener> mIRecordListenerSet = new CopyOnWriteArraySet<>();

    public BaseRecord(){

    }

    @Override
    public void addRecordListener(IRecordListener recordListener) {
        if(recordListener != null){
            mIRecordListenerSet.add(recordListener);
        }
    }

    @Override
    public void removeRecordListener(IRecordListener recordListener) {
        if(recordListener != null){
            mIRecordListenerSet.remove(recordListener);
        }
    }

    protected void notifyOnStartRecord(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IRecordListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IRecordListener next = iterator.next();
                    if(next != null){
                        next.onStartRecord(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnStopRecord(final String filePath){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IRecordListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IRecordListener next = iterator.next();
                    if(next != null){
                        next.onStopRecord(filePath);
                    }
                }
            }
        });
    }

    protected void notifyOnRecordException(final String filePath, final int errorCode, final String errorMsg){
        MainLooper.instance().post(new Runnable() {
            @Override
            public void run() {
                Iterator<IRecordListener> iterator = mIRecordListenerSet.iterator();
                while(iterator.hasNext()){
                    IRecordListener next = iterator.next();
                    if(next != null){
                        next.onRecordException(filePath, errorCode, errorMsg);
                    }
                }
            }
        });
    }
}
