package droidaudio.apollo.edus.com.droidaudio.multimedia;

import android.content.Context;

import droidaudio.apollo.edus.com.droidaudio.file.FileUtils;
import droidaudio.apollo.edus.com.droidaudio.multimedia.audio.PcmAudioPlay;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlay;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.OpusAudioTrack;
import droidaudio.apollo.edus.com.droidaudio.multimedia.media.StatedMediaPlay;

/**
 * Created by panda on 2018/1/1.
 */

public class PlayFactory {

    private volatile static PlayFactory sInstance;

    private Context mContext;
    private boolean mInited;

    public static PlayFactory getInstance() {
        if (sInstance == null) {
            synchronized (PlayFactory.class) {
                if (sInstance == null) {
                    sInstance = new PlayFactory();
                }
            }
        }
        return sInstance;
    }

    private PlayFactory() {

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

    public IPlay getPlayerByUrl(String url){
        String fileSuffix = FileUtils.getFileSuffix(url, false);
        if(fileSuffix == null){
            return new StatedMediaPlay();
        }else if(RecordType.OPUS.getSuffix().toLowerCase().equals(fileSuffix.toLowerCase())){
            return new OpusAudioTrack();
        }else if(RecordType.PCM.getSuffix().toLowerCase().equals(fileSuffix.toLowerCase())){
            return new PcmAudioPlay();
        }
        return new StatedMediaPlay();
    }


}
