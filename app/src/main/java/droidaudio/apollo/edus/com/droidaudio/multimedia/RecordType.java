package droidaudio.apollo.edus.com.droidaudio.multimedia;

/**
 * 录音的类型
 * Created by panda on 2018/1/1.
 */

public enum RecordType {

    AMR("amr"),
    OPUS("ogg"),
    PCM("pcm");

    private String suffix;

    public String getSuffix(){
        return suffix;
    }

    RecordType(String suffix){
        this.suffix = suffix;
    }

}
