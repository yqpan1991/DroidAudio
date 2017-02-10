package droidaudio.apollo.edus.com.droidaudio.media.audio;

import android.database.Cursor;
import android.provider.MediaStore;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import droidaudio.apollo.edus.com.droidaudio.media.IPlayerListener;

/**
 * Created by PandaPan on 2017/2/8.
 */

public class MediaPlayerWrapperTest extends InstrumentationTestCase {
    private final String TAG = "MediaPlayerWrapperTest";

    //测试播放
    private StatedMediaPlayer mMediaPlayerWrapper;
    private int calltimes;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testPlayNull() {
        mMediaPlayerWrapper = new StatedMediaPlayer();
        calltimes = 0;
        mMediaPlayerWrapper.setPlayerListener(new IPlayerListener() {
            @Override
            public void onPreparing(String filePath) {
                assertEquals("onPreparing not called", "onPreparing called");
            }

            @Override
            public void onPrepared(String filePath) {
                assertEquals("onPrepared not called", "onPrepared called");
            }

            @Override
            public void onPause(String filePath) {
                assertEquals("onPause not called", "onPause called");
            }

            @Override
            public void onPlay(String filePath) {
                assertEquals("onPlay not called", "onPlay called");
            }

            @Override
            public void onError(String filePath, int what, int extra) {
                calltimes++;
                assertEquals(1, calltimes);
            }

            @Override
            public void onStopped(String filePath) {
                assertEquals("onStopped not called", "onStopped called");
            }

            @Override
            public void onComplete(String filePath) {
                assertEquals("onComplete not called", "onComplete called");
            }

            @Override
            public void onProgressChanged(String filePath, int curPosition, int duration) {
                assertEquals("onProgressChanged not called", "onProgressChanged called");
            }
        });
        mMediaPlayerWrapper.start(null);
    }

    public void testLocalPath(){
        //列出手机中的音频文件
        List<MediaInfo> mediaList = getMediaList();
        if(mediaList != null){
            Log.e(TAG, "mediaList:"+mediaList);
        }else{
            Log.e(TAG, "no media");
        }

    }

    private List<MediaInfo> getMediaList(){
        Cursor cursor = getInstrumentation().getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor != null){
            List<MediaInfo> list = new ArrayList<>();
            try{
                if(cursor.getCount() > 0){
                    while(cursor.moveToNext()){
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        if(!TextUtils.isEmpty(path)){
                            MediaInfo info = new MediaInfo();
                            info.path = path;
                            info.title = title;
                            list.add(info);
                        }
                    }
                }
            }catch (Exception ex){

            }finally {
                cursor.close();
            }
            return list;
        }
        return null;
    }


    private class MediaInfo{
        String path;
        String title;

        @Override
        public String toString() {
            return "MediaInfo{" +
                    "path='" + path + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    public void testOnlinePath(){

    }
}
