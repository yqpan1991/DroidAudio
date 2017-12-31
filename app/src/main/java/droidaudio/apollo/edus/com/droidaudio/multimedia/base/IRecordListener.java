package droidaudio.apollo.edus.com.droidaudio.multimedia.base;

/**
 * Created by panda on 2017/12/17.
 */

public interface IRecordListener {
    int ERROR_UNKNOWN_ERROR = 0;
    int ERROR_NO_RECORD_PERMISSION = 1;
    int ERROR_FILE_GENERATE_ERROR = 2;
    int ERROR_FILE_WRITE_EXCEPTION = 3;
    int ERROR_INNER_AUDIO_RECORD = 4;
    int ERROR_RECORD_START_EXCEPTION = 5;
    int ERROR_RECORD_TYPE_NOT_SUPPORTED= 6;

    void onStartRecord(String filePath);

    /**
     * 只有内部在收到外部的停止的通知时,才会向外通知录音停止了,异常时,不会向外通知这个回调
     * @param filePath
     */
    void onStopRecord(String filePath);

    /**
     * 在录音发生异常时,向外回调这个错误,但是不会再回调onStopRecord(),外部在收到onRecordException()后,需要主动stopRecord()
     * @param filePath
     * @param errorCode
     * @param errorMsg
     */
    void onRecordException(String filePath, int errorCode, String errorMsg);
}
