package droidaudio.apollo.edus.com.droidaudio.multimedia;

import android.content.Context;

import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.OpusAudioRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.PcmAudioRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IRecord;
import droidaudio.apollo.edus.com.droidaudio.multimedia.media.MediaRecordWrapper;

/**
 * Created by panda on 2018/1/1.
 */

public class RecordFactory {
    private volatile static RecordFactory sInstance;
    private Context mContext;
    private boolean mInited;

    public static RecordFactory getInstance() {
        if (sInstance == null) {
            synchronized (RecordFactory.class) {
                if (sInstance == null) {
                    sInstance = new RecordFactory();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context){
        if(!mInited){
            mInited = true;
            mContext = context;
            if(mContext == null){
                throw new RuntimeException("RecordFactory init params cannot be null");
            }
        }

    }

    private RecordFactory() {

    }

    public IRecord getRecorder(RecordType recordType){
        if(recordType == null){
            return new MediaRecordWrapper(mContext);
        }
        if(recordType == RecordType.AMR){
            return new MediaRecordWrapper(mContext);
        }else if(recordType == RecordType.PCM){
            return new PcmAudioRecord(mContext);
        }else if(recordType == RecordType.OPUS){
            return new OpusAudioRecord(mContext);
        }
        return new MediaRecordWrapper(mContext);
    }

}
